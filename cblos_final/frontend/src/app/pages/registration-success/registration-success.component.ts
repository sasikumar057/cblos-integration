import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-registration-success',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container my-5 text-center">
      <div class="card p-5 shadow-sm mx-auto" style="max-width: 600px;">
        <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
        <h2 class="mt-3 fw-bold">Registration Submitted</h2>
        <p class="text-muted">
          Your corporate profile is now <strong>PENDING_VERIFICATION</strong>.
          A bank administrator will review your application before you can access loan products.
        </p>
        <a routerLink="/" class="btn btn-primary mt-3">Return to Corporate Hub</a>
      </div>
    </div>
  `
})
export class RegistrationSuccessComponent {}
