# 03 - Inventory Management

## Included Files

- `src/main/java/com/laundry/controller/InventoryServlet.java`
- `src/main/java/com/laundry/service/InventoryService.java`
- `src/main/java/com/laundry/dao/InventoryDAO.java`
- `src/main/java/com/laundry/model/InventoryItem.java`
- `src/main/java/com/laundry/model/InventoryTransaction.java`
- `src/main/webapp/WEB-INF/views/inventory/`

## Main Features

- Add and update inventory items
- Deactivate inventory items
- Record stock-in, usage, and adjustment transactions
- Automatically update stock balances
- Display low-stock items

## Depends On

- Order Management when stock usage is linked to an order
- `shared-core` for role authorization and database access
- `database-sqlserver` tables: `inventory_items`, `inventory_transactions`,
  `laundry_orders`, `users`
