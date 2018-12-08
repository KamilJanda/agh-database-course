import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int productId;
    private String ProductName;
    private int UnitsOnStock;

    @ManyToOne
    @JoinColumn(name = "SUPPLIER_FK")
    private Supplier supplierID;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_FK")
    private Category categoryID;

    @ManyToMany(mappedBy = "productsInOrder", cascade = CascadeType.PERSIST)
    private Set<Invoice> invoices = new HashSet<>();

//    @ManyToMany(mappedBy = "products")
//    private Set<Order> orders = new HashSet<>();

    public Product() {
    }

    Product(String productName, int unitsOnStock) {
        ProductName = productName;
        UnitsOnStock = unitsOnStock;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getUnitsOnStock() {
        return UnitsOnStock;
    }

    public void setUnitsOnStock(int unitsOnStock) {
        UnitsOnStock = unitsOnStock;
    }

    public Category getCategoryID() {
        return categoryID;
    }

    void setCategoryID(Category categoryID) {
        this.categoryID = categoryID;
        if (!this.categoryID.getProducts().contains(this)) {
            this.categoryID.getProducts().add(this);
        }
    }

    public Supplier getSupplierID() {
        return supplierID;
    }

    void setSupplierID(Supplier supplierID) {
        this.supplierID = supplierID;
        this.supplierID.getProducts().add(this);
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void addToInvoice(Invoice invoice, int quantity) {
        invoice.addProduct(this, quantity);
    }

//    public Set<Order> getOrders() {
//        return orders;
//    }
//
//    public void addOrder(Order order) {
//        this.orders.add(order);
//        order.getProducts().add(this);
//    }

    @Override
    public String toString() {
        return this.getProductName();
    }
}