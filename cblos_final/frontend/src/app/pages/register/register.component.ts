import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink , ActivatedRoute} from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';


@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent  implements OnInit {

  private readonly route = inject(ActivatedRoute);
ngOnInit(): void {
    // 🟢 Listen for incoming URL parameter: /register?email=user@domain.com
    this.route.queryParams.subscribe(params => {
      const emailParam = params['email'];
      
      if (emailParam) {
        // 1. Inject email into your Reactive Form Control
        this.registrationForm.patchValue({
          companyEmail: emailParam.trim()
        });
        
        // 2. Trigger your correction logic sequence right away!
        this.onEmailBlur();
      }
    });
  }
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);

  // Signals to track correction state
  isCorrectionMode = signal(false);
  existingCustomerId = signal<number | null>(null);
  rejectionReasonMessage = signal('');
  
  isLoading = signal(false);
  errorMessage = signal('');

  registrationForm = this.fb.group({
    companyName: ['', Validators.required],
    taxId: ['', Validators.required],
    companyEmail: ['', [Validators.required, Validators.email]],
    phoneNumber: [''],
    industryType: [''],
    tempRegistrationPassword: [''], // Will conditionally handle validation on submit
    businessAddress: ['']
  });

  // 🟢 Automatically check status when email field changes focus
  onEmailBlur(): void {
    const emailControl = this.registrationForm.get('companyEmail');
    if (emailControl?.invalid || !emailControl?.value) return;

    console.log('🔍 Checking registration status for:', emailControl.value);

    // 🟢 FIXED: Forcing "res: any" completely stops TypeScript syntax compilation errors
    this.customerService.checkRegistrationStatus(emailControl.value).subscribe({
      next: (res: any) => {
        console.log('📦 Status Response received:', res);
        const normalizedStatus = (res.status ?? '').toUpperCase();
        
        if (res.found && normalizedStatus.startsWith('REJECTED')) {
          this.isCorrectionMode.set(true);
          this.existingCustomerId.set(res.id ?? null);
          this.rejectionReasonMessage.set(res.rejectionReason ?? 'Invalid documentation provided.');
          
          this.registrationForm.get('tempRegistrationPassword')?.clearValidators();
          this.registrationForm.get('tempRegistrationPassword')?.updateValueAndValidity();

          console.log('🏢 Auto-filling profile from status response object...');
          this.registrationForm.patchValue({
            companyName: res.companyName || '',
            taxId: res.taxId ? res.taxId.toUpperCase().trim() : '',
            phoneNumber: res.phoneNumber || '',
            industryType: res.industryType || '',
            businessAddress: res.businessAddress || ''
          });

          this.registrationForm.markAsDirty();
          this.registrationForm.markAsTouched();
          this.registrationForm.updateValueAndValidity();
          
          console.log('✅ Form values auto-filled successfully:', this.registrationForm.value);

        } else if (res.found && normalizedStatus === 'PENDING_VERIFICATION') {
          this.errorMessage.set('An application under this email is already awaiting verification.');
        } else {
          this.resetCorrectionState();
        }
      },
      error: (err) => console.error('❌ Initial status check failed:', err)
    });
  }

  onSubmit(): void {
    if (this.registrationForm.invalid) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    const payload = this.registrationForm.value as any;

    // 🟢 Force a live lookup right on submit to determine the correct route
    this.customerService.checkRegistrationStatus(payload.companyEmail).subscribe({
      next: (statusRes: any) => {
        const normalizedStatus = (statusRes.status ?? '').toUpperCase();

        if (statusRes.found && normalizedStatus.startsWith('REJECTED')) {
          // 🟢 Route A: It exists and is rejected -> Run the PATCH update route
          const customerId = statusRes.id;
          
          if (!customerId) {
            this.isLoading.set(false);
            this.errorMessage.set('System error: Profile tracking identity missing.');
            return;
          }

          this.customerService.updateDetails(customerId, payload).subscribe({
            next: () => {
              this.isLoading.set(false);
              this.router.navigate(['/register/success']);
            },
            error: (err) => {
              this.isLoading.set(false);
              this.errorMessage.set(err.error?.message ?? 'Could not resubmit corrected details.');
            }
          });

        } else if (statusRes.found && normalizedStatus === 'PENDING_VERIFICATION') {
          this.isLoading.set(false);
          this.errorMessage.set('An application under this email is already awaiting verification.');
          
        } else {
          // 🟢 Route B: Brand new customer -> Run the normal onboarding POST route
          if (!payload.tempRegistrationPassword) {
            this.isLoading.set(false);
            this.errorMessage.set('Portal Password is required for new accounts.');
            return;
          }

          this.customerService.onboard(payload).subscribe({
            next: () => {
              this.isLoading.set(false);
              this.router.navigate(['/register/success']);
            },
            error: (err) => {
              this.isLoading.set(false);
              this.errorMessage.set(err.error?.message ?? 'Registration failed.');
            }
          });
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Could not verify registration routing status.');
      }
    });
  }

  private resetCorrectionState(): void {
    this.isCorrectionMode.set(false);
    this.existingCustomerId.set(null);
    this.rejectionReasonMessage.set('');
    this.errorMessage.set('');
  }
}