import com.sun.tools.corba.se.idl.constExpr.Or;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import javax.persistence.*;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;


public class Main {
    private static SessionFactory sessionFactory = null;

    private static EntityManager entityManager;


    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("myDatabaseConfig");
        entityManager = entityManagerFactory.createEntityManager();
        createData();

        OrderMenu orderMenu = new OrderMenu(entityManager);
        orderMenu.display();

        entityManager.close();
    }





    private static List<Product> getProductsFromCategory(String categoryName) {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Category category = entityManager
                .createQuery("From Category where name=:categoryName", Category.class)
                .setParameter("categoryName", categoryName)
                .getSingleResult();

        entityTransaction.commit();

        return category.getProducts();
    }

    private static Category getCategoryOfProduct(String productName) {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Product product = entityManager
                .createQuery("FROM Product WHERE ProductName=:productName", Product.class)
                .setParameter("productName", productName)
                .getSingleResult();

        entityTransaction.commit();

        return product.getCategoryID();
    }

    private static Set<Product> getProductsFromInvoice(int invoiceNumber) {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Invoice invoice = entityManager.createQuery("from Invoice where InvoiceNumber=:invoiceNumber", Invoice.class)
                .setParameter("invoiceNumber", invoiceNumber)
                .getSingleResult();

        entityTransaction.commit();
        return invoice.getProductsInOrder();
    }


    private static void createData() {
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Supplier sup = new Supplier("supplier super company",
                "Main st.",
                "New York",
                "zipcode",
                "123456");

        Customer customer = new Customer("some customer1", "wall st.", "NY", "zip code 1", 10);
        Customer customer1 = new Customer("super customer1", "main st.", "NY", "zip code 1", 0);
        Customer customer2 = new Customer("bad customer1", "some st.", "Warsaw", "zip code 4", 20);


        Product prod1 = new Product("random product", 20);
        Product prod2 = new Product("Super product", 40);
        Product prod3 = new Product("bad product", 10);
        Product prod4 = new Product("super cool product", 40);

        Category cat1 = new Category("category 1");
        Category cat2 = new Category("category 2");
        Category cat3 = new Category("category 3");

        sup.addProduct(prod1);
        sup.addProduct(prod2);
        sup.addProduct(prod3);
        sup.addProduct(prod4);

        cat1.addProducts(prod1);
        cat1.addProducts(prod3);

        cat2.addProducts(prod4);
        cat3.addProducts(prod2);

        Invoice inv1 = new Invoice();
        Invoice inv2 = new Invoice();

        inv1.addProduct(prod1, 5);
        inv1.addProduct(prod2, 6);
        inv1.addProduct(prod3, 5);

        inv2.addProduct(prod4, 2);

//        entityManager.persist(prod1);
//        entityManager.persist(prod2);
//        entityManager.persist(prod3);
//        entityManager.persist(prod4);

//        Order order = new Order();
//        order.setCustomer(customer);
//        order.addProduct(prod1);
//
//        entityManager.persist(order);

        entityManager.persist(cat1);
        entityManager.persist(cat2);
        entityManager.persist(cat3);

        entityManager.persist(sup);

        entityManager.persist(customer);
        entityManager.persist(customer1);
        entityManager.persist(customer2);

        entityManager.persist(inv1);
        entityManager.persist(inv2);

        entityTransaction.commit();

    }

    private static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
        }
        return sessionFactory;
    }
}