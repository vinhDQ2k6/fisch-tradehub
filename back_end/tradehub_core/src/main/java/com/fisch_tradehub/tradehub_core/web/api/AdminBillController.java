package com.fisch_tradehub.tradehub_core.web.api;

import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.service.BillService;
import com.fisch_tradehub.tradehub_core.web.dto.BillDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin bill management operations.
 */
@RestController
@RequestMapping("/api/admin/bills")
@RequiredArgsConstructor
public class AdminBillController {

  private final BillService billService;

  /**
   * Get all bills, optionally filtered by status.
   *
   * @param status optional filter by bill status
   * @return list of bills
   */
  @GetMapping
  public ResponseEntity<List<BillDTO>> getAllBills(
    @RequestParam(required = false) BillStatus status
  ) {
    if (status != null) {
      return ResponseEntity.ok(billService.getBillsByStatus(status));
    }
    return ResponseEntity.ok(billService.getAllBills());
  }

  /**
   * Get a specific bill by ID.
   *
   * @param id the bill ID
   * @return the bill details
   */
  @GetMapping("/{id}")
  public ResponseEntity<BillDTO> getBillById(@PathVariable Long id) {
    return ResponseEntity.ok(billService.getBillById(id));
  }

  /**
   * Complete a bill (mark as COMPLETED).
   *
   * @param id the bill ID
   * @return the updated bill details
   */
  @PostMapping("/{id}/complete")
  public ResponseEntity<BillDTO> completeBill(@PathVariable Long id) {
    return ResponseEntity.ok(billService.completeBill(id));
  }

  /**
   * Cancel a bill (admin can cancel PENDING_PAYMENT or PROCESSING bills).
   *
   * @param id the bill ID
   * @return the updated bill details
   */
  @PostMapping("/{id}/cancel")
  public ResponseEntity<BillDTO> cancelBill(@PathVariable Long id) {
    return ResponseEntity.ok(billService.adminCancelBill(id));
  }
}
