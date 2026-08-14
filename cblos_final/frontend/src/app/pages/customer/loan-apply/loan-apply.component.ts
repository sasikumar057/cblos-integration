import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoanService } from '../../../core/services/loan.service';
import { LoanProduct, LoanProductService } from '../../../core/services/loan-product.service';

@Component({
  selector: 'app-loan-apply',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './loan-apply.component.html'
})
export class LoanApplyComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly loanService = inject(LoanService);
  private readonly productService = inject(LoanProductService);
  private readonly fb = inject(FormBuilder);

  customerId = 0;
  productType = '';
  product = signal<LoanProduct | null>(null);
  error = signal('');

  form = this.fb.group({
    loanAmount: [null as number | null, [Validators.required, Validators.min(1)]],
    requestedTenureMonths: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.customerId = Number(this.route.snapshot.paramMap.get('customerId'));
    this.productType = this.route.snapshot.queryParamMap.get('productType') ?? '';
    this.productService.getCatalog().subscribe(products => {
      const selected = products.find(p => p.productName === this.productType) ?? null;
      this.product.set(selected);
      if (selected) {
        this.form.controls.loanAmount.setValidators([
          Validators.required,
          Validators.min(selected.minLoanAmount ?? 1),
          Validators.max(selected.maxLoanAmount ?? Number.MAX_SAFE_INTEGER)
        ]);
        this.form.controls.requestedTenureMonths.setValidators([
          Validators.required,
          Validators.min(selected.minTenureMonths ?? 1),
          Validators.max(selected.maxTenureMonths ?? Number.MAX_SAFE_INTEGER)
        ]);
        this.form.updateValueAndValidity();
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loanService.submit(this.customerId, {
      loanType: this.productType,
      loanAmount: this.form.value.loanAmount!,
      requestedTenureMonths: this.form.value.requestedTenureMonths!
    }).subscribe({
      next: (app) => this.router.navigate(['/customer/dashboard/application', app.applicationId, 'collateral']),
      error: (err) => this.error.set(err.error?.message ?? 'Failed to submit application.')
    });
  }
}
