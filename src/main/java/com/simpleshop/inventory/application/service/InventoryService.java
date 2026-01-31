package com.simpleshop.inventory.application.service;

import com.simpleshop.inventory.application.command.*;
import com.simpleshop.inventory.application.port.in.*;
import com.simpleshop.inventory.application.port.out.StockRepository;
import com.simpleshop.inventory.application.port.out.WarehouseRepository;
import com.simpleshop.inventory.application.query.*;
import com.simpleshop.inventory.domain.exception.InsufficientStockException;
import com.simpleshop.inventory.domain.exception.StockNotFoundException;
import com.simpleshop.inventory.domain.exception.WarehouseNotFoundException;
import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.Warehouse;
import com.simpleshop.inventory.domain.model.vo.StockId;
import com.simpleshop.inventory.domain.model.vo.WarehouseId;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Quantity;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService implements CreateWarehouseUseCase, ReplenishStockUseCase,
        ReserveStockUseCase, ReleaseStockUseCase, GetWarehouseUseCase,
        CheckStockAvailabilityUseCase, ListWarehouseStockUseCase, AllocateStockUseCase,
        ConfirmStockReservationUseCase {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(StockRepository stockRepository, WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @NewSpan("inventory.createWarehouse")
    public WarehouseView create(CreateWarehouseCommand command) {
        Address address = Address.of(
            command.street(),
            command.city(),
            command.postalCode(),
            command.country()
        );
        Warehouse warehouse = Warehouse.create(command.name(), address);
        warehouse = warehouseRepository.save(warehouse);
        return toWarehouseView(warehouse);
    }

    @Override
    @NewSpan("inventory.replenishStock")
    public StockView replenish(ReplenishStockCommand command) {
        if (!warehouseRepository.existsById(WarehouseId.of(command.warehouseId()))) {
            throw new WarehouseNotFoundException(command.warehouseId());
        }

        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseGet(() -> Stock.create(command.productId(), command.warehouseId(), Quantity.zero()));

        stock.replenish(Quantity.of(command.quantity()));
        stock = stockRepository.save(stock);
        return toStockView(stock);
    }

    @Override
    @NewSpan("inventory.reserveStock")
    public void reserve(ReserveStockCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.reserve(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }

    @Override
    @NewSpan("inventory.releaseStock")
    public void release(ReleaseStockCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.release(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }

    @Override
    @Transactional(readOnly = true)
    @NewSpan("inventory.getWarehouse")
    public Optional<WarehouseView> get(GetWarehouseQuery query) {
        return warehouseRepository.findById(WarehouseId.of(query.warehouseId()))
            .map(this::toWarehouseView);
    }

    @Override
    @Transactional(readOnly = true)
    @NewSpan("inventory.checkStockAvailability")
    public ProductAvailabilityView check(CheckStockAvailabilityQuery query) {
        int totalAvailable;
        int totalReserved;

        if (query.warehouseId() != null) {
            Stock stock = stockRepository.findByProductIdAndWarehouseId(query.productId(), query.warehouseId())
                .orElse(null);
            totalAvailable = stock != null ? stock.getAvailableQuantity().getValue() : 0;
            totalReserved = stock != null ? stock.getReservedQuantity().getValue() : 0;
        } else {
            totalAvailable = stockRepository.sumAvailableByProductId(query.productId());
            totalReserved = stockRepository.sumReservedByProductId(query.productId());
        }

        return new ProductAvailabilityView(query.productId(), totalAvailable, totalReserved);
    }

    @Override
    @Transactional(readOnly = true)
    @NewSpan("inventory.listWarehouseStock")
    public Page<StockView> list(GetWarehouseStockQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size());
        return stockRepository.findByWarehouseId(query.warehouseId(), pageable)
            .map(this::toStockView);
    }

    private WarehouseView toWarehouseView(Warehouse warehouse) {
        return new WarehouseView(
            warehouse.getId(),
            warehouse.getName().getValue(),
            warehouse.getAddress().getStreet(),
            warehouse.getAddress().getCity(),
            warehouse.getAddress().getPostalCode(),
            warehouse.getAddress().getCountry(),
            warehouse.isActive()
        );
    }

    private StockView toStockView(Stock stock) {
        return new StockView(
            stock.getId(),
            stock.getProductId(),
            stock.getWarehouseId(),
            stock.getQuantity().getValue(),
            stock.getReservedQuantity().getValue(),
            stock.getAvailableQuantity().getValue()
        );
    }

    @Override
    @NewSpan("inventory.allocateStock")
    public StockAllocationResult allocate(AllocateStockCommand command) {
        List<StockAllocationResult.Allocation> allocations = new ArrayList<>();
        List<Stock> stocksToSave = new ArrayList<>();
        
        for (AllocateStockCommand.AllocationRequest request : command.items()) {
            List<Stock> availableStocks = stockRepository.findByProductId(request.productId());
            
            Stock selectedStock = availableStocks.stream()
                .filter(s -> s.getAvailableQuantity().getValue() >= request.quantity())
                .max(Comparator.comparingInt(s -> s.getAvailableQuantity().getValue()))
                .orElseThrow(() -> {
                    int totalAvailable = availableStocks.stream()
                        .mapToInt(s -> s.getAvailableQuantity().getValue())
                        .sum();
                    return new InsufficientStockException(request.productId(), totalAvailable, request.quantity());
                });
            
            selectedStock.reserve(Quantity.of(request.quantity()));
            stocksToSave.add(selectedStock);
            
            allocations.add(new StockAllocationResult.Allocation(
                request.productId(),
                selectedStock.getWarehouseId(),
                request.quantity()
            ));
        }
        
        for (Stock stock : stocksToSave) {
            stockRepository.save(stock);
        }
        
        return new StockAllocationResult(allocations);
    }

    @Override
    @NewSpan("inventory.confirmStockReservation")
    public void confirm(ConfirmStockReservationCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.confirmReservation(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }
}
