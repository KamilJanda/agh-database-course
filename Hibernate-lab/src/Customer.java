import javax.persistence.Entity;

@Entity
public class Customer extends Company {


    private int discount;

    public Customer() {
        super();
    }

    public Customer(String companyName, String street, String city, String zipCode, int discount) {
        super(companyName, street, city, zipCode);
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }


}
