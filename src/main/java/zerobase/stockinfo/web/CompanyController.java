package zerobase.stockinfo.web;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.persist.entity.CompanyEntity;
import zerobase.stockinfo.service.CompanyService;

@RestController
@AllArgsConstructor
@RequestMapping("/company")
public class CompanyController {

  private final CompanyService companyService;

  @GetMapping("/autocomplete")
  public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
    var result = this.companyService.getCompanyNamesByKeyword(keyword);
    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<?> searchCompany(final Pageable pageable) {
    Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
    return ResponseEntity.ok(companies);
  }

  @PostMapping
  public ResponseEntity<?> addCompany(@RequestBody Company request) {
    String ticker = request.getTicker().trim();
    if (ObjectUtils.isEmpty(ticker)) {
      throw new RuntimeException("Ticker is empty");
    }
    Company company = this.companyService.save(ticker);
    this.companyService.addAutocompleteKeyword(company.getName());

    return ResponseEntity.ok(company);
  }

  @DeleteMapping
  public ResponseEntity<?> deleteCompany(){
    return null;
  }
}