import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  user = { username: '', email: '', password: '' };
  message = '';

  constructor(private authService: AuthService, private router: Router) {}

register() {
  this.authService.register(this.user).subscribe({
    next: () => {
      // Directly redirect after successful registration
      this.router.navigate(['/login']);
    },
    error: (err: any) => {
      console.log('Registration error:', err);
      if (typeof err.error === 'string') {
        this.message = err.error;
      } else if (err.error?.message) {
        this.message = err.error.message;
      } else {
        this.message = 'Registration failed';
      }
    }
  });
}

}
