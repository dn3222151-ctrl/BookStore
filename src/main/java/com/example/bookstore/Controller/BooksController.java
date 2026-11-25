package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Request.BookRequest;
import com.example.bookstore.Response.BooksReponse;
import com.example.bookstore.Service.BooksService;
import com.example.bookstore.Service.CategoriesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BooksController {

    @Autowired
    private BooksService booksService;

    @Autowired
    private CategoriesService categoriesService;

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/hienthi")
    public String hienThi(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "categoryId", required = false) Integer categoryId,
                          Model model,
                          HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        List<BooksReponse> listBook;
        if ((keyword != null && !keyword.trim().isEmpty()) || (categoryId != null && categoryId > 0)) {
            listBook = booksService.searchAndFilter(keyword, categoryId)
                    .stream()
                    .map(BooksReponse::new)
                    .toList();
        } else {
            listBook = booksService.getAllBooks();
        }

        model.addAttribute("listBook", listBook);
        model.addAttribute("listcategories", categoriesService.getAllCategories());
        model.addAttribute("newBook", new BookRequest());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "DashBoardBooks";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public BooksReponse getById(@PathVariable int id) {
        Books book = booksService.getById(id);
        return book != null ? new BooksReponse(book) : null;
    }

    @GetMapping("/add")
    public String showAddPage(Model model, HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("newBook", new BookRequest());
        model.addAttribute("listcategories", categoriesService.getAllCategories());
        return "Add/addBooks";
    }

    @PostMapping("/add")
    public String addBooks(@ModelAttribute("newBook") BookRequest bookRequest,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }
        try {
            booksService.addBooks(bookRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sách mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sách: " + e.getMessage());
        }
        return "redirect:/books/hienthi";
    }

    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") int id, Model model, HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }
        Books book = booksService.getById(id);
        if (book == null) {
            return "redirect:/books/hienthi";
        }
        BookRequest request = new BookRequest();
        request.setId(book.getId());
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setPrice(book.getPrice());
        request.setQuantity(book.getQuantity());
        request.setDescription(book.getDescription());
        request.setImageUrl(book.getImageUrl());
        if (book.getCategories() != null) {
            request.setCategoryId(book.getCategories().getId());
        }

        model.addAttribute("editBook", request);
        model.addAttribute("listcategories", categoriesService.getAllCategories());
        return "EditBook";
    }

    @PostMapping("/update")
    public String updateBooks(@ModelAttribute BookRequest bookRequest,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }
        try {
            booksService.updateBooks(bookRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật sách: " + e.getMessage());
        }
        return "redirect:/books/hienthi";
    }

    @GetMapping("/delete/{id}")
    public String removeBooks(@PathVariable("id") Integer id,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }
        try {
            booksService.deleteBooks(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sách vì sách đã có trong đơn hàng!");
        }
        return "redirect:/books/hienthi";
    }
}
