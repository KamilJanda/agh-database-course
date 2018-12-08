import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer InvoiceNumber;

    private Integer Quantity = 0;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Product> productsInOrder = new HashSet<>();

    public Invoice() {
    }

    public Invoice(int quantity) {
        Quantity = quantity;
    }

    public void addProduct(Product product, int quantity) {
        productsInOrder.add(product);
        product.getInvoices().add(this);
        this.Quantity += quantity;
    }

    public Set<Product> getProductsInOrder(){
        return productsInOrder;
    }

    public Integer getInvoiceNumber() {
        return InvoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.InvoiceNumber = invoiceNumber;
    }

    public Integer getQuantity() {
        return Quantity;
    }

    public void setQuantity(Integer quantity) {
        Quantity = quantity;
    }
}
