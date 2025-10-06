import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot.html',
  styleUrls: ['./forgot.css']
})
export class ForgotComponent {
  email = '';
  message = '';

  constructor(private authService: AuthService, private router: Router) { }


  sendOtp() {
    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: () => {
        this.message = '✅ OTP sent successfully!';
        this.router.navigate(['/reset'], { state: { email: this.email } });
      },
      error: (err: any) => {
        console.error('Forgot password error:', err);
        this.message = err.error?.message || '❌ Failed to send OTP';
      }
    });
  }

}
