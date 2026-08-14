import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoanAccountService, LoanAccount } from '../../../core/services/loan-account.service';
import { RepaymentService, RepaymentSchedule } from '../../../core/services/repayment.service';
import { DecimalPipe, DatePipe } from '@angular/common';
import { finalize, forkJoin } from 'rxjs';

@Component({
  selector: 'app-loan-repayment',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './loan-repayment.component.html',
  styleUrl: './loan-repayment.component.css'
})
export class LoanRepaymentComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly accountService = inject(LoanAccountService);
  private readonly repaymentService = inject(RepaymentService);


  showSuccessPopup = signal(false);

  accountId = 0;
  customerId = 0;
  account = signal<LoanAccount | null>(null);
  schedule = signal<RepaymentSchedule[]>([]);
  unpaid = signal<RepaymentSchedule[]>([]);
  selectedInstallmentId = signal<number | null>(null);
  paymentType = signal<'MONTHLY' | 'FULL'>('MONTHLY');
  message = signal('');
  error = signal('');
  isLoading = signal(false);
  isPaying = signal(false);

  selectedInstallment = computed(() =>
    this.unpaid().find(i => i.id === this.selectedInstallmentId()) ?? null
  );

  totalOutstanding = computed(() =>
    this.unpaid().reduce((total, inst) => total + Number(inst.installmentAmount ?? 0), 0)
  );

  ngOnInit(): void {
    this.accountId = Number(this.route.snapshot.paramMap.get('accountId'));
    this.customerId = this.auth.currentUser()?.corporateCustomerId ?? 0;
    this.loadRepaymentData();
  }

  loadRepaymentData(): void {
    this.error.set('');
    this.isLoading.set(true);
    forkJoin({
      account: this.accountService.getById(this.accountId),
      schedule: this.repaymentService.getSchedule(this.accountId)
    }).pipe(finalize(() => this.isLoading.set(false))).subscribe({
next: ({ account, schedule }) => {
  this.account.set(account);
  this.schedule.set(schedule);
  
  const unpaid = schedule.filter(i => {
    const status = (i.status ?? '').toUpperCase();
    return status === 'PENDING' || status === 'UNPAID';
  }).sort((a, b) => (a.installmentNumber ?? 0) - (b.installmentNumber ?? 0)); // 🟢 Guard: Always sort chronologically

  this.unpaid.set(unpaid);
  
  // 🟢 Automatically locking selection to month index 0
  this.selectedInstallmentId.set(unpaid[0]?.id ?? null);
},
      error: () => this.error.set('Repayment details could not be loaded.')
    });
  }

  selectInstallment(value: string): void {
    const id = Number(value);
    this.selectedInstallmentId.set(Number.isFinite(id) && id > 0 ? id : null);
  }

  changePaymentType(value: string): void {
    this.paymentType.set(value === 'FULL' ? 'FULL' : 'MONTHLY');
    this.error.set('');
  }

 pay(): void {
    this.error.set('');

    if (this.paymentType() === 'FULL') {
      if (this.unpaid().length === 0) {
        this.error.set('No unpaid installments are available for settlement.');
        return;
      }
      this.isPaying.set(true);
      this.repaymentService.settleAccount(this.accountId)
        .pipe(finalize(() => this.isPaying.set(false))).subscribe({
          next: (msg) => {
            this.message.set(msg);
            
            // 🟢 UX FIX: Show success popup first, then navigate after 2.5 seconds
            this.showSuccessPopup.set(true);
            setTimeout(() => {
              this.showSuccessPopup.set(false);
              this.router.navigate(['/customer/dashboard', this.customerId]);
            }, 2500);
          },
          error: () => this.error.set('Full settlement failed.')
        });
      return;
    }

    const installmentId = this.selectedInstallmentId();
    if (!installmentId) {
      this.error.set('No installment selected.');
      return;
    }
    this.isPaying.set(true);
    this.repaymentService.payInstallment(this.accountId, installmentId)
      .pipe(finalize(() => this.isPaying.set(false))).subscribe({
      next: (msg) => {
        this.message.set(msg);
        
        // 🟢 UX FIX: Show success popup first, then navigate after 2.5 seconds
        this.showSuccessPopup.set(true);
        setTimeout(() => {
          this.showSuccessPopup.set(false);
          this.router.navigate(['/customer/dashboard', this.customerId]);
        }, 3000);
      },
      error: () => this.error.set('Payment failed.')
    });
  }
}
