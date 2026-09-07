import { Component, OnInit, inject, signal, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-application-success',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container my-5 text-center">
      <div class="card p-5 shadow-sm mx-auto" style="max-width: 600px;">
        <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
        <h2 class="mt-3 fw-bold">Application Submitted</h2>
        <p class="text-muted">Your loan application has been submitted for underwriting review.</p>
        <p class="small text-muted">Redirecting to dashboard in {{ countdown() }}s...</p>
        <a [routerLink]="['/customer/dashboard', customerId]" class="btn btn-primary mt-2">Go to Dashboard</a>
      </div>
    </div>
  `
})
export class ApplicationSuccessComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  customerId = 0;
  countdown = signal(5);
  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.customerId = Number(this.route.snapshot.paramMap.get('customerId'));
    this.timer = setInterval(() => {
      const next = this.countdown() - 1;
      this.countdown.set(next);
      if (next <= 0) {
        this.router.navigate(['/customer/dashboard', this.customerId]);
      }
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }
}
