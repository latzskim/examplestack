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
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @WithSpan("inventory.createWarehouse")
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
    @WithSpan("inventory.replenishStock")
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
    @WithSpan("inventory.reserveStock")
    public void reserve(ReserveStockCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.reserve(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }

    @Override
    @WithSpan("inventory.releaseStock")
    public void release(ReleaseStockCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.release(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan("inventory.getWarehouse")
    public Optional<WarehouseView> get(GetWarehouseQuery query) {
        return warehouseRepository.findById(WarehouseId.of(query.warehouseId()))
            .map(this::toWarehouseView);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan("inventory.checkStockAvailability")
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
    @WithSpan("inventory.checkManyStockAvailability")
    public Map<UUID, ProductAvailabilityView> checkMany(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Integer> totalAvailableByProduct = stockRepository.sumAvailableByProductIds(productIds);
        Map<UUID, Integer> totalReservedByProduct = stockRepository.sumReservedByProductIds(productIds);

        Map<UUID, ProductAvailabilityView> result = new LinkedHashMap<>();
        for (UUID productId : productIds) {
            result.put(productId, new ProductAvailabilityView(
                productId,
                totalAvailableByProduct.getOrDefault(productId, 0),
                totalReservedByProduct.getOrDefault(productId, 0)
            ));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan("inventory.listWarehouseStock")
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
    @WithSpan("inventory.allocateStock")
    public StockAllocationResult allocate(AllocateStockCommand command) {
        List<StockAllocationResult.Allocation> allocations = new ArrayList<>();
        List<Stock> stocksToSave = new ArrayList<>();

        List<UUID> productIds = command.items().stream()
            .map(AllocateStockCommand.AllocationRequest::productId)
            .distinct()
            .toList();
        Map<UUID, List<Stock>> availableStocksByProduct = stockRepository.findByProductIds(productIds).stream()
            .collect(Collectors.groupingBy(Stock::getProductId));

        for (AllocateStockCommand.AllocationRequest request : command.items()) {
            List<Stock> availableStocks = availableStocksByProduct.getOrDefault(request.productId(), List.of());
            
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
    @WithSpan("inventory.confirmStockReservation")
    public void confirm(ConfirmStockReservationCommand command) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
            .orElseThrow(() -> new StockNotFoundException(command.productId(), command.warehouseId()));

        stock.confirmReservation(Quantity.of(command.quantity()));
        stockRepository.save(stock);
    }
}
