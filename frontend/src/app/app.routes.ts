import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';
import { HomeComponent } from './features/home/home';
import { adminGuard } from './admin.guard';
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard';
import { AdminUsersComponent } from './features/admin/admin-users/admin-users';
import { AdminPostsComponent } from './features/admin/admin-posts/admin-posts';
import { AdminReportsComponent } from './features/admin/admin-reports/admin-reports';
import { UserComponent } from './features/user/user';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: HomeComponent },
  { path: 'profile/:id', component: UserComponent },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'reports', component: AdminReportsComponent },
      { path: 'posts', component: AdminPostsComponent }

    ]
  },
];
