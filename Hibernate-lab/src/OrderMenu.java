import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class OrderMenu {

    private Scanner userInput = new Scanner(System.in);
    private EntityManager entityManager;

    public OrderMenu(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    void makeOrder() {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Order order = new Order();

        List<Customer> customers = entityManager
                .createQuery("from Customer", Customer.class)
                .getResultList();

        List<Product> products = entityManager
                .createQuery("from Product", Product.class)
                .getResultList();

        customers.forEach(System.out::println);

        userInput = new Scanner(System.in);

        System.out.println("Select customer: ");
        String cutomerName = userInput.nextLine();

        Optional<Customer> optionaCustomer = customers
                .stream()
                .filter(customer1 -> customer1.getCompanyName().equals(cutomerName))
                .findFirst();

        Customer customer = null;

        if (optionaCustomer.isPresent()) {
            customer = optionaCustomer.get();
        } else {
            errorInput(entityTransaction, "no customer in base");
            return;
        }
        order.setCustomer(customer);

        int i=1;
        for (Product prod:products) {
            System.out.println(i+". "+prod.getProductName());
            i++;
        }

        int input = 0;

        while(true) {
            System.out.println("Chooser product or Insert 0 to finish order");
            input = userInput.nextInt();
            if (input == 0) break;
            order.addProduct(products.get(input-1));
        }

        entityManager.persist(order);
        entityTransaction.commit();
    }

    void display(){
        while (true){
            System.out.println("Insert 1 to make order");
            System.out.println("Insert 0 to EXIT");
            int input = userInput.nextInt();

            if(input==1){
                makeOrder();
            }
            else break;
        }
    }

    private void errorInput(EntityTransaction entityTransaction, String reason) {
        System.out.println("Cannot make order because " + reason);
        entityTransaction.rollback();
    }
}
