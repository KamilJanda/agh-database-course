import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Supplier extends Company{

    private String bankAccountNumber;

    @OneToMany
    @JoinColumn(name = "SUPPLIER_FK")
    private Set<Product> products = new HashSet<>();

    public Supplier() {
        super();
    }

    public Supplier(String companyName, String street, String city, String zipCode, String bankAccountNumber) {
        super(companyName, street, city, zipCode);
        this.bankAccountNumber = bankAccountNumber;
    }

    public Set<Product> getProducts() {
        return products;
    }

    void addProduct(Product product) {
        this.products.add(product);
        product.setSupplierID(this);
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
}