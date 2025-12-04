package com.fisch_tradehub.tradehub_core.web.api;

import com.fisch_tradehub.tradehub_core.service.BillService;
import com.fisch_tradehub.tradehub_core.web.dto.BillDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

  private final BillService billService;

  @PostMapping("/checkout")
  public ResponseEntity<BillDTO> checkout(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    BillDTO bill = billService.checkout(userDetails.getUsername());
    return ResponseEntity.ok(bill);
  }

  @GetMapping
  public ResponseEntity<List<BillDTO>> getMyBills(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(billService.getMyBills(userDetails.getUsername()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BillDTO> getBill(@PathVariable Long id) {
    return ResponseEntity.ok(billService.getBillById(id));
  }

  @PostMapping("/{id}/pay")
  public ResponseEntity<BillDTO> payBill(
    @PathVariable Long id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(
      billService.payBill(id, userDetails.getUsername())
    );
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<BillDTO> cancelBill(
    @PathVariable Long id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(
      billService.cancelBill(id, userDetails.getUsername())
    );
  }
  // Admin endpoints moved to AdminBillController:
  // GET /api/admin/bills - getAllBills
  // GET /api/admin/bills/{id} - getBillById
  // POST /api/admin/bills/{id}/complete - completeBill
  // POST /api/admin/bills/{id}/cancel - adminCancelBill
}
