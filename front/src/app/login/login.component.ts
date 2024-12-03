import { Component, inject, signal } from '@angular/core';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { CenteredContainerComponent } from '../../atomic-design/centered-container/centered-container.component';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { InputComponent } from '../../atomic-design/input/input.component';
import { Router, RouterLink } from '@angular/router';
import { notBlankValidator } from '../utils/validator.utils';
import { DefaultComponent } from '../abstract-default.component';
import { api } from '../http.service';
import { PATH_REGISTER, PATH_USER, route } from '../app.routes';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'ins-login',
  imports: [
    ButtonComponent,
    CenteredContainerComponent,
    FormsModule,
    InputComponent,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent extends DefaultComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  errorMessage = signal<string | undefined>(undefined);

  form = this.formBuilder.nonNullable.group({
    username: this.formBuilder.nonNullable.control<string>('', [
      Validators.required,
      notBlankValidator,
    ]),
    password: this.formBuilder.nonNullable.control<string>('', [
      Validators.required,
      notBlankValidator,
    ]),
  });

  protected readonly PATH_REGISTER = PATH_REGISTER;
  protected readonly route = route;

  login() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set(undefined);
      return;
    }
    const credentials = this.form.getRawValue();
    const body = new HttpParams()
      .set('username', credentials.username)
      .set('password', credentials.password)
      .toString();
    fetch(api('login'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      },
      body,
    }).then((response) => {
      if (!response.ok) {
        this.errorMessage.set('Compte inexistant ou mot de passe incorrect.');
        return;
      }
      this.httpService.isAuthenticated.set(true);
      this.router.navigate([PATH_USER]);
    });
  }
}
