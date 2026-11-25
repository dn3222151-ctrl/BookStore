package com.example.bookstore;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.Categories;
import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.BooksRepository;
import com.example.bookstore.Repository.CategoriesRepository;
import com.example.bookstore.Repository.OrdersRepository;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrdersService ordersService;

    @Override
    public void run(String... args) throws Exception {
        // 1. Khởi tạo tài khoản người dùng nếu chưa có
        User adminUser = null;
        User customerUser = null;

        if (userRepository.count() == 0) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("123456");
            adminUser.setFullName("Quản Trị Viên Hệ Thống");
            adminUser.setEmail("admin@bookstore.com");
            adminUser.setRole("ADMIN");
            adminUser = userRepository.save(adminUser);

            customerUser = new User();
            customerUser.setUsername("khachhang");
            customerUser.setPassword("123456");
            customerUser.setFullName("Nguyễn Văn Khách");
            customerUser.setEmail("khachhang@gmail.com");
            customerUser.setRole("USER");
            customerUser = userRepository.save(customerUser);

            System.out.println("✅ Đã khởi tạo tài khoản mặc định: admin/123456 (ADMIN) và khachhang/123456 (USER)");
        } else {
            adminUser = userRepository.findByUsername("admin").orElse(null);
            customerUser = userRepository.findByUsername("khachhang").orElse(null);
        }

        // 2. Khởi tạo thêm thể loại nếu thiếu
        Categories itCat = categoriesRepository.findByNameContainingIgnoreCase("IT").stream().findFirst().orElse(null);
        if (itCat == null) {
            itCat = new Categories();
            itCat.setName("IT & Lập trình");
            itCat = categoriesRepository.save(itCat);
        }

        Categories mangaCat = categoriesRepository.findByNameContainingIgnoreCase("Manga").stream().findFirst().orElse(null);
        if (mangaCat == null) {
            mangaCat = new Categories();
            mangaCat.setName("Manga & Truyện tranh");
            mangaCat = categoriesRepository.save(mangaCat);
        }

        Categories kinhTeCat = categoriesRepository.findByNameContainingIgnoreCase("Kinh tế").stream().findFirst().orElse(null);
        if (kinhTeCat == null) {
            kinhTeCat = new Categories();
            kinhTeCat.setName("Kinh tế & Đầu tư");
            kinhTeCat = categoriesRepository.save(kinhTeCat);
        }

        Categories vanHocCat = categoriesRepository.findByNameContainingIgnoreCase("Văn học").stream().findFirst().orElse(null);
        if (vanHocCat == null) {
            vanHocCat = new Categories();
            vanHocCat.setName("Văn học & Nghệ thuật");
            vanHocCat = categoriesRepository.save(vanHocCat);
        }

        // 3. Khởi tạo sách nếu DB đang trống
        if (booksRepository.count() == 0) {
            Books b1 = new Books();
            b1.setTitle("Lập trình Java từ Cơ bản đến Nâng cao");
            b1.setAuthor("Robert C. Martin");
            b1.setPrice(185000);
            b1.setQuantity(50);
            b1.setDescription("Cuốn sách toàn diện hướng dẫn lập trình Java từ cú pháp cơ bản, hướng đối tượng OOP đến Spring Boot và Design Patterns hiện đại.");
            b1.setImageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60");
            b1.setCategories(itCat);
            booksRepository.save(b1);

            Books b2 = new Books();
            b2.setTitle("Spring Boot Microservices Thực Chiến");
            b2.setAuthor("Craig Walls");
            b2.setPrice(245000);
            b2.setQuantity(35);
            b2.setDescription("Xây dựng kiến trúc hệ thống phân tán với Spring Boot, Spring Cloud, Eureka, Kafka và bảo mật với JWT.");
            b2.setImageUrl("https://images.unsplash.com/photo-1532012164546-f432f2e3edd9?w=500&auto=format&fit=crop&q=60");
            b2.setCategories(itCat);
            booksRepository.save(b2);

            Books b3 = new Books();
            b3.setTitle("One Piece - Tập 100: Haoshoku");
            b3.setAuthor("Eiichiro Oda");
            b3.setPrice(35000);
            b3.setQuantity(120);
            b3.setDescription("Trận chiến quyết định tại đảo Onigashima giữa băng Mũ Rơm và Tứ Hoàng Kaido cùng Big Mom bước vào giai đoạn kịch tính nhất.");
            b3.setImageUrl("https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&auto=format&fit=crop&q=60");
            b3.setCategories(mangaCat);
            booksRepository.save(b3);

            Books b4 = new Books();
            b4.setTitle("Thám Tử Lừng Danh Conan - Tập 102");
            b4.setAuthor("Gosho Aoyama");
            b4.setPrice(30000);
            b4.setQuantity(90);
            b4.setDescription("Những vụ án hóc búa mới thử thách khả năng suy luận phi thường của Edogawa Conan và đồng nghiệp.");
            b4.setImageUrl("https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&auto=format&fit=crop&q=60");
            b4.setCategories(mangaCat);
            booksRepository.save(b4);

            Books b5 = new Books();
            b5.setTitle("Dạy Con Làm Giàu (Tập 1)");
            b5.setAuthor("Robert T. Kiyosaki");
            b5.setPrice(125000);
            b5.setQuantity(60);
            b5.setDescription("Cuốn sách kinh điển về tư duy tài chính, phân biệt tài sản và tiêu sản giúp bạn đạt được tự do tài chính bền vững.");
            b5.setImageUrl("https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=500&auto=format&fit=crop&q=60");
            b5.setCategories(kinhTeCat);
            booksRepository.save(b5);

            Books b6 = new Books();
            b6.setTitle("Nhà Giả Kim (The Alchemist)");
            b6.setAuthor("Paulo Coelho");
            b6.setPrice(89000);
            b6.setQuantity(80);
            b6.setDescription("Hành trình của chàng chăn cừu Santiago đi tìm kho báu kim tự tháp Ai Cập và bài học lắng nghe tiếng gọi trái tim.");
            b6.setImageUrl("https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=60");
            b6.setCategories(vanHocCat);
            booksRepository.save(b6);

            System.out.println("✅ Đã khởi tạo dữ liệu mẫu 6 cuốn sách vào cơ sở dữ liệu!");

            // 4. Khởi tạo 1 đơn hàng mẫu cho customer
            if (ordersRepository.count() == 0 && customerUser != null) {
                Map<Integer, Integer> sampleCart = new HashMap<>();
                sampleCart.put(b1.getId(), 1);
                sampleCart.put(b3.getId(), 2);
                Orders sampleOrder = ordersService.createOrder(customerUser.getId(), "Số 123 Đường Cầu Giấy, Hà Nội", sampleCart);
                sampleOrder.setStatus("CONFIRMED");
                ordersRepository.save(sampleOrder);
                System.out.println("✅ Đã khởi tạo 1 đơn hàng mẫu thử nghiệm!");
            }
        }
    }
}

