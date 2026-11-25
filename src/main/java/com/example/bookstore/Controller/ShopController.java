package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Response.CartItem;
import com.example.bookstore.Service.BooksService;
import com.example.bookstore.Service.CategoriesService;
import com.example.bookstore.Service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private UserRepository userRepository;

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
    public String shopCatalog(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "categoryId", required = false) Integer categoryId,
                              Model model,
                              HttpSession session) {
        List<Books> books = booksService.searchAndFilter(keyword, categoryId);

        model.addAttribute("books", books);
        model.addAttribute("categories", categoriesService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

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
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "BookDetail";
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
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm \"" + book.getTitle() + "\" vào giỏ hàng!");
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

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartCount", getCartItemCount(session));
        model.addAttribute("currentUser", currentUser);

        return "Checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("fullName") String fullName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("address") String address,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> cart = getCartFromSession(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng rỗng!");
            return "redirect:/shop";
        }

        User currentUser = (User) session.getAttribute("currentUser");
        Integer userId = null;

        if (currentUser != null) {
            userId = currentUser.getId();
        } else {
            // Nếu người mua chưa có tài khoản, tìm hoặc tạo tài khoản khách mặc định
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

            // Xóa sạch giỏ hàng sau khi đặt thành công
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

