import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { GlobalSearchComponent } from '../../shared/components/global-search/global-search.component';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';
import { DemoBannerComponent } from '../../shared/components/demo-banner/demo-banner.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

interface NavItem { label: string; icon: string; route: string; }
interface NavSection { title: string; items: NavItem[]; }

const SECTIONS: NavSection[] = [
  { title: 'Overview', items: [
    { label: 'Buy → Build → Sell', icon: 'bi-arrow-repeat', route: '/workflow' },
    { label: 'Dashboard', icon: 'bi-grid-1x2', route: '/dashboard' },
  ]},
  { title: 'Company Setup', items: [
    { label: 'Organizations', icon: 'bi-building', route: '/master-data/organizations' },
    { label: 'Factories', icon: 'bi-houses', route: '/master-data/factories' },
    { label: 'Vehicle Models', icon: 'bi-car-front', route: '/master-data/vehicle-models' },
    { label: 'Vehicle Options', icon: 'bi-sliders', route: '/vehicle-config' },
  ]},
  { title: 'Operations', items: [
    { label: 'Parts & Build Steps', icon: 'bi-diagram-3', route: '/engineering' },
    { label: 'Suppliers & Orders', icon: 'bi-truck', route: '/supply-chain' },
    { label: 'Stock & Parts', icon: 'bi-box-seam', route: '/inventory' },
    { label: 'Build Orders', icon: 'bi-clipboard-check', route: '/production' },
    { label: 'Work Schedule', icon: 'bi-calendar3', route: '/scheduling' },
    { label: 'Quality Checks', icon: 'bi-shield-check', route: '/quality' },
    { label: 'Vehicle History', icon: 'bi-fingerprint', route: '/vin-traceability' },
  ]},
  { title: 'Commercial & Support', items: [
    { label: 'Sales & Customers', icon: 'bi-credit-card', route: '/sales-payments' },
    { label: 'Vehicle Yard', icon: 'bi-geo-alt', route: '/finished-vehicles' },
    { label: 'Approval Queues', icon: 'bi-check2-square', route: '/approval-queues' },
    { label: 'Workers & Shifts', icon: 'bi-people', route: '/workforce' },
    { label: 'Machine Repairs', icon: 'bi-tools', route: '/maintenance' },
    { label: 'Safety', icon: 'bi-shield-exclamation', route: '/safety' },
    { label: 'Reports', icon: 'bi-graph-up', route: '/reports' },
    { label: 'Audit Log', icon: 'bi-journal-text', route: '/audit-log' },
  ]},
];

@Component({
  selector: 'vx-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, GlobalSearchComponent, NotificationBellComponent, DemoBannerComponent, ConfirmDialogComponent],
  template: `
    <div class="vx-app-shell">
      <!-- Sidebar -->
      <aside class="vx-sidebar vx-glass" [class.collapsed]="sidebarCollapsed()">
        <div class="vx-sidebar-header d-flex align-items-center gap-2 px-3">
          <div class="vx-logo-badge-sm"><i class="bi bi-lightning-charge-fill"></i></div>
          @if (!sidebarCollapsed()) {
            <div class="flex-grow-1">
              <div class="vx-sidebar-brand">VeynaX <span class="vx-text-gradient">AMMS</span></div>
              <div class="vx-sidebar-version">v0.3 · Angular 22</div>
            </div>
          }
          <button class="vx-icon-btn" (click)="sidebarCollapsed.set(!sidebarCollapsed())" title="Toggle sidebar">
            <i class="bi" [class.bi-chevron-left]="!sidebarCollapsed()" [class.bi-chevron-right]="sidebarCollapsed()"></i>
          </button>
        </div>

        <nav class="vx-sidebar-nav flex-grow-1 overflow-auto px-2">
          @for (section of sections; track section.title) {
            @if (!sidebarCollapsed()) {
              <div class="vx-sidebar-section">{{ section.title }}</div>
            }
            @for (item of section.items; track item.route) {
              <a class="nav-link vx-nav-item" [class.collapsed]="sidebarCollapsed()"
                 routerLink="{{ item.route }}" routerLinkActive="active"
                 [title]="item.label">
                <i class="bi {{ item.icon }} vx-nav-icon"></i>
                @if (!sidebarCollapsed()) { <span class="vx-nav-label">{{ item.label }}</span> }
              </a>
            }
          }
        </nav>

        @if (!sidebarCollapsed()) {
          <div class="vx-sidebar-footer px-3 py-2">
            <div class="vx-glass p-2 rounded" style="border-radius:var(--vx-radius-sm);">
              <div class="small" style="color:var(--vx-text-faint);font-size:.68rem;">One factory · one digital thread</div>
            </div>
          </div>
        }
      </aside>

      <!-- Main column -->
      <div class="vx-main-col d-flex flex-column">
        <vx-demo-banner></vx-demo-banner>
        <header class="vx-topbar vx-glass d-flex align-items-center gap-3 px-3 px-md-4 py-2">
          <span class="fw-bold d-md-none" style="font-size:.95rem;">VeynaX AMMS</span>
          <div class="ms-auto d-flex align-items-center gap-2">
            <div class="d-none d-md-block"><vx-global-search></vx-global-search></div>
            <button class="vx-icon-btn" (click)="toggleTheme()" [title]="theme()==='dark'?'Switch to light':'Switch to dark'">
              <i class="bi" [class.bi-moon-stars]="theme()==='dark'" [class.bi-sun]="theme()==='light'"></i>
            </button>
            <vx-notification-bell></vx-notification-bell>
            <div class="d-none d-sm-flex align-items-center gap-2 px-2 py-1 vx-glass rounded" style="border-radius:999px;">
              <div class="vx-avatar">{{ initials() }}</div>
              <div class="text-start" style="line-height:1.2;">
                <div class="fw-semibold" style="font-size:.78rem;">{{ auth.currentUser()?.fullName || 'User' }}</div>
                <div style="font-size:.65rem;color:var(--vx-text-faint);">{{ auth.currentUser()?.role || '—' }}</div>
              </div>
            </div>
            <button class="btn btn-sm btn-outline-secondary" (click)="auth.logout()">
              <i class="bi bi-box-arrow-right me-1"></i><span class="d-none d-sm-inline">Logout</span>
            </button>
          </div>
        </header>

        <main class="vx-main-content p-3 p-md-4 flex-grow-1">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>

    <!-- Toasts -->
    <vx-confirm-dialog></vx-confirm-dialog>
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index:1080;">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast show vx-animate-in" role="alert"
             style="border-left:4px solid {{ t.type==='success'?'var(--vx-success)':t.type==='error'?'var(--vx-danger)':'var(--vx-accent)' }};">
          <div class="toast-body d-flex justify-content-between align-items-center">
            <span class="small">{{ t.message }}</span>
            <button class="btn-close ms-2" (click)="toast.dismiss(t.id)"></button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .vx-app-shell { display: flex; min-height: 100vh; position: relative; z-index: 1; }
    .vx-sidebar {
      width: 252px; flex-shrink: 0; display: flex; flex-direction: column;
      border-right: 1px solid var(--vx-border);
      transition: width var(--vx-transition);
      position: sticky; top: 0; height: 100vh;
    }
    .vx-sidebar.collapsed { width: 72px; }
    .vx-sidebar-header { padding-top: 1rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--vx-border); }
    .vx-logo-badge-sm {
      width: 36px; height: 36px; border-radius: 10px; flex-shrink: 0;
      background: linear-gradient(135deg, var(--vx-accent-from), var(--vx-accent-to));
      color: #fff; display: inline-flex; align-items: center; justify-content: center;
      font-size: 1rem; box-shadow: 0 4px 14px var(--vx-accent-glow);
    }
    .vx-sidebar-brand { font-size: .92rem; font-weight: 700; letter-spacing: -0.02em; }
    .vx-sidebar-version { font-size: .62rem; color: var(--vx-text-faint); letter-spacing: .06em; }
    .vx-icon-btn {
      width: 34px; height: 34px; border-radius: 9px; border: 1px solid var(--vx-border);
      background: var(--vx-surface); color: var(--vx-text-muted); cursor: pointer;
      display: inline-flex; align-items: center; justify-content: center;
      transition: all var(--vx-transition); flex-shrink: 0;
    }
    .vx-icon-btn:hover { background: var(--vx-surface-hover); color: var(--vx-text); border-color: var(--vx-border-strong); }
    .vx-sidebar-nav { padding-top: 0.5rem; }
    .vx-sidebar-section {
      font-size: .62rem; text-transform: uppercase; letter-spacing: .12em; font-weight: 700;
      color: var(--vx-text-faint); padding: 0.9rem 0.6rem 0.4rem;
    }
    .vx-nav-item {
      display: flex; align-items: center; gap: 10px; padding: 0.5rem 0.65rem;
      margin-bottom: 2px; font-size: .84rem; font-weight: 500; text-decoration: none;
    }
    .vx-nav-item.collapsed { justify-content: center; padding: 0.55rem; }
    .vx-nav-icon { font-size: 1rem; width: 20px; text-align: center; flex-shrink: 0; opacity: 0.85; }
    .vx-main-col { flex: 1; min-width: 0; display: flex; flex-direction: column; }
    .vx-topbar {
      position: sticky; top: 0; z-index: 100;
      border-bottom: 1px solid var(--vx-border);
      min-height: 56px;
    }
    .vx-main-content { position: relative; z-index: 1; max-width: 1600px; width: 100%; margin: 0 auto; }
    .vx-avatar {
      width: 28px; height: 28px; border-radius: 50%; flex-shrink: 0;
      background: linear-gradient(135deg, var(--vx-accent-from), var(--vx-accent-to));
      color: #fff; font-size: .7rem; font-weight: 700;
      display: inline-flex; align-items: center; justify-content: center;
    }
    .vx-sidebar-footer { border-top: 1px solid var(--vx-border); margin-top: auto; }
    ::-webkit-scrollbar { width: 6px; }
  `]
})
export class AdminLayoutComponent {
  sections = SECTIONS;
  sidebarCollapsed = signal(false);
  theme = signal<'dark' | 'light'>('dark');

  constructor(public auth: AuthService, public toast: ToastService) {}

  initials() {
    const name = this.auth.currentUser()?.fullName || 'U';
    return name.split(' ').map(p => p[0]).slice(0, 2).join('').toUpperCase();
  }

  toggleTheme() {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.setAttribute('data-theme', next);
  }
}
