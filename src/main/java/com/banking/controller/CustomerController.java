package com.banking.controller;

import com.banking.model.Customer;
import com.banking.model.Transaction;
import com.banking.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired private CustomerService customerService;

    @GetMapping("/register")
    public String registerPage() { return "customer/register"; }

    @PostMapping("/register")
    public String submitKyc(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String pan,
                             @RequestParam String aadhaar,
                             RedirectAttributes ra) {
        try {
            customerService.submitKyc(fullName, email, pan, aadhaar);
            ra.addFlashAttribute("success", "KYC submitted! You'll receive login credentials once approved.");
            return "redirect:/customer/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/register";
        }
    }

    @GetMapping("/login")
    public String loginPage() { return "customer/login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String accountNumber,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes ra) {
        try {
            Customer c = customerService.authenticate(accountNumber.trim(), password);
            session.setAttribute("CUSTOMER", c);
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customer/login?logout=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Customer c = (Customer) session.getAttribute("CUSTOMER");
        if (c == null) return "redirect:/customer/login";
        Customer fresh = customerService.findByAccountNumber(c.getAccountNumber()).orElse(c);
        session.setAttribute("CUSTOMER", fresh);
        List<Transaction> last5 = customerService.getLast5(fresh.getAccountNumber());
        model.addAttribute("customer", fresh);
        model.addAttribute("recentTx", last5);
        return "customer/dashboard";
    }

    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model) {
        Customer c = (Customer) session.getAttribute("CUSTOMER");
        if (c == null) return "redirect:/customer/login";
        Customer fresh = customerService.findByAccountNumber(c.getAccountNumber()).orElse(c);
        model.addAttribute("customer", fresh);
        return "customer/transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam String recipientAccount,
                             @RequestParam double amount,
                             @RequestParam(defaultValue = "Fund Transfer") String description,
                             HttpSession session,
                             RedirectAttributes ra) {
        Customer c = (Customer) session.getAttribute("CUSTOMER");
        if (c == null) return "redirect:/customer/login";
        try {
            customerService.transfer(c.getAccountNumber(), recipientAccount.trim(), amount, description);
            Customer fresh = customerService.findByAccountNumber(c.getAccountNumber()).orElse(c);
            session.setAttribute("CUSTOMER", fresh);
            ra.addFlashAttribute("success", "Transfer Successful!");
            return "redirect:/customer/transfer";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/transfer";
        }
    }

    // ─── Transaction History (FIXED) ──────────────────────────
    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {
        Customer c = (Customer) session.getAttribute("CUSTOMER");
        if (c == null) return "redirect:/customer/login";
        
        List<Transaction> txList = customerService.getTransactions(c.getAccountNumber());
        
        // Java side calculation to fix Thymeleaf Error
        double totalSpent = txList.stream()
                .filter(t -> t.getSenderAccountNumber().equals(c.getAccountNumber()))
                .mapToDouble(Transaction::getAmount)
                .sum();
                
        double totalReceived = txList.stream()
                .filter(t -> t.getRecipientAccountNumber().equals(c.getAccountNumber()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        model.addAttribute("customer", c);
        model.addAttribute("transactions", txList);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("totalReceived", totalReceived);
        
        return "customer/transactions";
    }
}