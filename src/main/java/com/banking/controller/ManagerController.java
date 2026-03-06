package com.banking.controller;

import com.banking.model.Customer;
import com.banking.model.Transaction;
import com.banking.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired private CustomerService customerService;

    // ─── Login ────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() { return "manager/login"; }

    // ─── Dashboard ────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Customer> all = customerService.getAllCustomers();
        long pending = all.stream().filter(c -> "PENDING".equals(c.getStatus())).count();
        long approved = all.stream().filter(c -> "APPROVED".equals(c.getStatus())).count();
        long frozen = all.stream().filter(c -> "FROZEN".equals(c.getStatus())).count();
        long totalTx = customerService.getAllTransactions().size();
        model.addAttribute("totalCustomers", all.size());
        model.addAttribute("pendingCount", pending);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("frozenCount", frozen);
        model.addAttribute("totalTransactions", totalTx);
        model.addAttribute("recentCustomers", all.subList(0, Math.min(5, all.size())));
        return "manager/dashboard";
    }

    // ─── KYC Pending ──────────────────────────────────────────────────
    @GetMapping("/kyc-pending")
    public String pendingKyc(Model model) {
        model.addAttribute("pendingList", customerService.getPendingKyc());
        return "manager/kyc-pending";
    }

    @PostMapping("/approve-kyc/{id}")
    public String approveKyc(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Map<String, String> result = customerService.approveKyc(id);
            ra.addFlashAttribute("approvalResult", result);
            ra.addFlashAttribute("success",
                String.format("KYC Approved! Account: %s | Temp Password: %s (sent to %s)",
                    result.get("accountNumber"), result.get("tempPassword"), result.get("email")));
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/kyc-pending";
    }

    // ─── All Customers ────────────────────────────────────────────────
    @GetMapping("/customers")
    public String allCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "manager/customers";
    }

    // ─── Freeze / Unfreeze ────────────────────────────────────────────
    @PostMapping("/toggle-freeze/{id}")
    public String toggleFreeze(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Customer c = customerService.toggleFreeze(id);
            ra.addFlashAttribute("success",
                "Account " + c.getAccountNumber() + " has been " + (c.isFrozen() ? "FROZEN" : "UNFROZEN"));
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/customers";
    }

    // ─── All Transactions ─────────────────────────────────────────────
    @GetMapping("/transactions")
    public String allTransactions(Model model) {
        List<Transaction> txList = customerService.getAllTransactions();
        model.addAttribute("transactions", txList);
        double totalVolume = txList.stream().mapToDouble(Transaction::getAmount).sum();
        model.addAttribute("totalVolume", totalVolume);
        return "manager/transactions";
    }
}
