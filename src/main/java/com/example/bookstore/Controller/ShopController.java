package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Entity.ViewHistory;
import com.example.bookstore.Repository.OrdersRepository;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Repository.ViewHistoryRepository;
import com.example.bookstore.Response.CartItem;
import com.example.bookstore.Service.BooksService;
import com.example.bookstore.Service.CategoriesService;
import com.example.bookstore.Service.CultivationHelper;
import com.example.bookstore.Service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private BooksService booksService;

    @Autowired
    private CategoriesService categoriesService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getCartFromSession(HttpSession session) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private int getCartItemCount(HttpSession session) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String shopCatalog(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "categoryId", required = false) Integer categoryId,
                              Model model,
                              HttpSession session) {
        List<Books> books = booksService.searchAndFilter(keyword, categoryId);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null) {
            User freshUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
            model.addAttribute("currentRealm", CultivationHelper.calculateForUser(
                    freshUser, ordersService.getOrdersByUserId(freshUser.getId())));
        }

        model.addAttribute("books", books);
        model.addAttribute("categories", categoriesService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "Shop";
    }

    @GetMapping("/book/{id}")
    public String bookDetail(@PathVariable("id") Integer id,
                             Model model,
                             HttpSession session) {
        Books book = booksService.getById(id);
        if (book == null) {
            return "redirect:/shop";
        }

        // Lưu lịch sử xem nếu đã đăng nhập
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            Optional<ViewHistory> existing = viewHistoryRepository.findByUserIdAndBookId(currentUser.getId(), book.getId());
            ViewHistory history = existing.orElseGet(ViewHistory::new);
            history.setUser(currentUser);
            history.setBook(book);
            history.setViewedAt(LocalDateTime.now());
            viewHistoryRepository.save(history);
        }

        List<Books> relatedBooks = Collections.emptyList();
        if (book.getCategories() != null) {
            relatedBooks = booksService.getBooksByCategory(book.getCategories().getId())
                    .stream()
                    .filter(b -> b.getId() != book.getId())
                    .limit(4)
                    .toList();
        }

        model.addAttribute("book", book);
        model.addAttribute("relatedBooks", relatedBooks);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "BookDetail";
    }

    @GetMapping("/history")
    public String viewHistory(@RequestParam(value = "page", defaultValue = "1") int page,
                              Model model,
                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?required=true";
        }

        int pageIndex = Math.max(0, page - 1);
        Page<ViewHistory> historyPage = viewHistoryRepository.findByUserIdOrderByViewedAtDesc(
                currentUser.getId(), PageRequest.of(pageIndex, 10)
        );

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "History";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("bookId") Integer bookId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "redirect", required = false) String redirectUrl,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Books book = booksService.getById(bookId);
        if (book != null) {
            if (book.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Sách này hiện đã hết hàng trong kho!");
                return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/shop";
            }
            Map<Integer, Integer> cart = getCartFromSession(session);
            int currentQty = cart.getOrDefault(bookId, 0);
            int newQty = Math.min(book.getQuantity(), currentQty + Math.max(1, quantity));
            cart.put(bookId, newQty);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm \"" + book.getTitle() + "\" vào kệ sách!");
        }

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/shop/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        List<CartItem> cartItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Books book = booksService.getById(entry.getKey());
            if (book != null) {
                CartItem item = new CartItem(book, entry.getValue());
                cartItems.add(item);
                totalAmount += item.getSubtotal();
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "Cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("bookId") Integer bookId,
                             @RequestParam("quantity") int quantity,
                             HttpSession session) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        if (quantity <= 0) {
            cart.remove(bookId);
        } else {
            Books book = booksService.getById(bookId);
            if (book != null) {
                int safeQty = Math.min(book.getQuantity(), quantity);
                cart.put(bookId, safeQty);
            }
        }
        return "redirect:/shop/cart";
    }

    @GetMapping("/cart/remove/{bookId}")
    public String removeFromCart(@PathVariable("bookId") Integer bookId,
                                 HttpSession session) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        cart.remove(bookId);
        return "redirect:/shop/cart";
    }

    @GetMapping("/checkout")
    @Transactional(readOnly = true)
    public String showCheckout(Model model, HttpSession session) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        if (cart.isEmpty()) {
            return "redirect:/shop";
        }

        List<CartItem> cartItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Books book = booksService.getById(entry.getKey());
            if (book != null) {
                CartItem item = new CartItem(book, entry.getValue());
                cartItems.add(item);
                totalAmount += item.getSubtotal();
            }
        }

        User currentUser = (User) session.getAttribute("currentUser");
        CultivationHelper.RealmInfo realm = (currentUser != null)
                ? CultivationHelper.calculateForUser(
                userRepository.findById(currentUser.getId()).orElse(currentUser),
                ordersService.getOrdersByUserId(currentUser.getId()))
                : CultivationHelper.calculateRealm(0, 0);

        double discountAmount = Math.round(totalAmount * realm.discountPercent / 100.0);
        double finalAmount = Math.max(0, totalAmount - discountAmount);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("realm", realm);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalAmount", finalAmount);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "Checkout";
    }

    @PostMapping("/checkout")
    @Transactional
    public String processCheckout(@RequestParam("fullName") String fullName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("address") String address,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kệ sách rỗng!");
            return "redirect:/shop";
        }

        User currentUser = (User) session.getAttribute("currentUser");
        Integer userId = null;
        CultivationHelper.RealmInfo realm = CultivationHelper.calculateRealm(0, 0);

        if (currentUser != null) {
            User freshUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
            userId = freshUser.getId();
            realm = CultivationHelper.calculateForUser(freshUser, ordersService.getOrdersByUserId(userId));
        } else {
            Optional<User> defaultUserOpt = userRepository.findByUsername("khachhang");
            if (defaultUserOpt.isPresent()) {
                userId = defaultUserOpt.get().getId();
            }
        }

        String fullShippingAddress = address + " (Người nhận: " + fullName + " - SĐT: " + phone + ")";
        if (notes != null && !notes.trim().isEmpty()) {
            fullShippingAddress += " [Ghi chú: " + notes.trim() + "]";
        }

        try {
            Orders order = ordersService.createOrder(userId, fullShippingAddress, cart);

            // Áp dụng giảm giá theo Cảnh giới Tu tiên
            if (realm.discountPercent > 0) {
                double discount = Math.round(order.getTotalAmount() * realm.discountPercent / 100.0);
                order.setTotalAmount(Math.max(0, order.getTotalAmount() - discount));
                order.setShippingAddress(order.getShippingAddress() + " [Ưu đãi " + realm.name + " -" + realm.discountPercent + "%]");
                ordersRepository.save(order);
            }

            session.removeAttribute("cart");

            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công!");
            return "redirect:/shop/order-success/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo đơn hàng: " + e.getMessage());
            return "redirect:/shop/checkout";
        }
    }

    @GetMapping("/order-success/{orderId}")
    public String orderSuccess(@PathVariable("orderId") Integer orderId,
                               Model model,
                               HttpSession session) {
        Orders order = ordersService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/shop";
        }

        model.addAttribute("order", order);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "OrderSuccess";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?required=true";
        }

        List<Orders> orders = ordersService.getOrdersByUserId(currentUser.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "MyOrders";
    }
}