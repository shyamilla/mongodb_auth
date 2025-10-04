import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reset',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset.html',
  styleUrls: ['./reset.css']
})
export class ResetComponent {
  email = history.state.email || '';
  otp = '';
  newPassword = '';
  message = '';

  constructor(private authService: AuthService, private router: Router) {}

  resetPassword() {
    this.authService.resetPassword({ email: this.email, otp: this.otp, newPassword: this.newPassword }).subscribe({
      next: () => {
        this.message = 'Password reset successfully!';
        this.router.navigate(['/login']);
      },
      error: (err: any) => this.message = err.error || 'Reset failed'
    });
  }
}
