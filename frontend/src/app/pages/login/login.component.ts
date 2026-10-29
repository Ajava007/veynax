import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ApiResponse } from '../../core/models/api-response';
import { AuthResponse } from '../../core/models/auth';
import { FieldErrorComponent } from '../../shared/components/field-error/field-error.component';

@Component({
  selector: 'vx-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FieldErrorComponent],
  template: `
    <div class="vx-login-shell">
      <!-- Left brand panel -->
      <div class="vx-brand-panel d-none d-lg-flex flex-column justify-content-between p-5">
        <div class="vx-brand-bg"></div>
        <div class="position-relative">
          <div class="d-flex align-items-center gap-3 mb-5">
            <div class="vx-logo-badge"><i class="bi bi-lightning-charge-fill"></i></div>
            <div>
              <div class="vx-brand-name">VeynaX <span class="vx-text-gradient">AMMS</span></div>
              <div class="vx-brand-sub">Automotive Manufacturing Management</div>
            </div>
          </div>
          <h1 class="vx-hero-title">One factory.<br>One digital <span class="vx-text-gradient">thread.</span></h1>
          <p class="vx-hero-sub">Orchestrate build orders, quality gates, maintenance, and supply chain — from raw casting to customer delivery.</p>
        </div>
        <div class="position-relative d-flex gap-4 flex-wrap">
          <div class="vx-hero-stat"><div class="vx-hero-stat-num">12</div><div class="vx-hero-stat-label">Modules</div></div>
          <div class="vx-hero-stat"><div class="vx-hero-stat-num">7</div><div class="vx-hero-stat-label">Roles</div></div>
          <div class="vx-hero-stat"><div class="vx-hero-stat-num">24h</div><div class="vx-hero-stat-label">Token</div></div>
        </div>
      </div>

      <!-- Right form panel -->
      <div class="vx-form-panel d-flex align-items-center justify-content-center p-4">
        <div class="vx-card vx-card-glow vx-login-card vx-animate-in">
          <div class="text-center mb-4">
            <div class="d-inline-flex align-items-center justify-content-center mb-3 vx-logo-badge vx-logo-badge-lg">
              <i class="bi bi-lightning-charge-fill"></i>
            </div>
            <h3 class="fw-bold mb-1">Welcome back</h3>
            <p class="mb-0 text-muted" style="font-size:.85rem;">Sign in to your factory console</p>
          </div>

          <ul class="nav nav-pills nav-fill mb-4">
            <li class="nav-item"><button class="nav-link" [class.active]="mode()==='login'" (click)="mode.set('login')">Sign in</button></li>
            <li class="nav-item"><button class="nav-link" [class.active]="mode()==='register'" (click)="mode.set('register')">Register</button></li>
          </ul>

          <form [formGroup]="form" (ngSubmit)="submit()">
            @if (mode() === 'register') {
              <div class="mb-3">
                <label class="form-label">Full name</label>
                <input type="text" class="form-control" formControlName="fullName" placeholder="Jane Engineer">
                <vx-field-error [control]="form.get('fullName')" />
              </div>
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" formControlName="email" placeholder="jane@veynax.com">
                <vx-field-error [control]="form.get('email')" />
              </div>
            }
            <div class="mb-3">
              <label class="form-label">Username</label>
              <input type="text" class="form-control" formControlName="username" placeholder="username" autocomplete="username">
              <vx-field-error [control]="form.get('username')" />
            </div>
            <div class="mb-4">
              <label class="form-label">Password</label>
              <input type="password" class="form-control" formControlName="password" placeholder="••••••••" autocomplete="current-password">
              <vx-field-error [control]="form.get('password')" />
            </div>
            <button type="submit" class="btn btn-primary w-100 vx-login-btn" [disabled]="loading()">
              @if (loading()) { <span class="spinner-border spinner-border-sm me-2"></span> }
              {{ mode() === 'login' ? 'Sign in to console' : 'Create account' }}
              @if (!loading()) { <i class="bi bi-arrow-right ms-2"></i> }
            </button>
          </form>

          <div class="mt-4 p-3 rounded vx-glass" style="border-radius:var(--vx-radius-sm);">
            <p class="small text-center mb-0"><i class="bi bi-shield-lock me-1"></i> Secured by JWT · roles enforced server-side</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .vx-login-shell { min-height: 100vh; display: flex; position: relative; z-index: 1; }
    .vx-brand-panel { flex: 1.1; position: relative; overflow: hidden; color: #f1f5f9; }
    .vx-brand-bg {
      position: absolute; inset: 0;
      background:
        radial-gradient(ellipse 60% 50% at 30% 20%, rgba(99,102,241,0.5), transparent 60%),
        radial-gradient(ellipse 50% 40% at 80% 80%, rgba(6,182,212,0.4), transparent 60%),
        linear-gradient(135deg, #0a0f1e 0%, #131a2e 50%, #0a0f1e 100%);
      animation: vx-bg-shift 12s ease-in-out infinite alternate;
    }
    @keyframes vx-bg-shift {
      0% { background-position: 0% 0%; }
      100% { background-position: 100% 100%; }
    }
    .vx-brand-bg::after {
      content: ''; position: absolute; inset: 0;
      background-image: radial-gradient(circle at 1px 1px, rgba(255,255,255,0.06) 1px, transparent 0);
      background-size: 32px 32px;
    }
    .vx-logo-badge {
      width: 44px; height: 44px; border-radius: 12px;
      background: linear-gradient(135deg, var(--vx-accent-from), var(--vx-accent-to));
      color: #fff; display: inline-flex; align-items: center; justify-content: center;
      font-size: 1.3rem; box-shadow: 0 8px 24px var(--vx-accent-glow);
    }
    .vx-logo-badge-lg { width: 60px; height: 60px; font-size: 1.7rem; border-radius: 16px; }
    .vx-brand-name { font-size: 1.2rem; font-weight: 700; letter-spacing: -0.02em; }
    .vx-brand-sub { font-size: 0.75rem; color: var(--vx-text-faint); letter-spacing: 0.08em; text-transform: uppercase; }
    .vx-hero-title { font-size: 2.6rem; font-weight: 800; line-height: 1.15; letter-spacing: -0.03em; margin-bottom: 1rem; }
    .vx-hero-sub { color: var(--vx-text-muted); font-size: 0.95rem; max-width: 420px; line-height: 1.6; }
    .vx-hero-stat-num { font-size: 1.8rem; font-weight: 700; letter-spacing: -0.02em; }
    .vx-hero-stat-label { font-size: 0.7rem; color: var(--vx-text-faint); text-transform: uppercase; letter-spacing: 0.1em; font-weight: 600; }
    .vx-form-panel { flex: 1; min-width: 380px; }
    .vx-login-card { width: 100%; max-width: 420px; padding: 2.2rem; }
    .vx-login-btn { padding: 0.75rem; font-size: 0.95rem; }
    @media (max-width: 992px) { .vx-form-panel { min-width: 100%; } .vx-login-card { padding: 1.5rem; } }
  `]
})
export class LoginComponent {
  mode = signal<'login' | 'register'>('login');
  loading = signal(false);

  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    fullName: [''],
    email: ['']
  });

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router, private toast: ToastService) {}

  submit() {
    if (this.form.invalid) { this.toast.error('Please fill in all required fields.'); return; }
    this.loading.set(true);
    const v = this.form.value;
    if (this.mode() === 'register' && (!v.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email))) {
      this.loading.set(false);
      this.toast.error('Enter a valid email address to register.');
      return;
    }
    const req$ = this.mode() === 'login'
      ? this.auth.login({ username: v.username!, password: v.password! })
      : this.auth.register({
          username: v.username!,
          email: v.email || '',
          firstName: (v.fullName || '').trim().split(/\s+/)[0] || undefined,
          lastName: (v.fullName || '').trim().split(/\s+/).slice(1).join(' ') || undefined,
          password: v.password!
        });
    req$.subscribe({
      next: (res: ApiResponse<AuthResponse>) => {
        this.loading.set(false);
        if (!res.success) { this.toast.error(res.message || 'Login failed.'); return; }
        if (this.mode() === 'register') {
          this.toast.success(res.message || 'Account created. Please sign in.');
          this.mode.set('login');
          this.form.patchValue({ password: '' });
          return;
        }
        this.toast.success('Welcome, ' + (this.auth.currentUser()?.fullName ?? 'user'));
        this.router.navigate(['/dashboard']);
      },
      error: () => this.loading.set(false)
    });
  }
}
