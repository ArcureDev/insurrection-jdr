import { Component, inject, signal } from '@angular/core';
import { DefaultComponent } from '../abstract-default.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { notBlankValidator } from '../utils/validator.utils';
import { InputComponent } from '../../atomic-design/input/input.component';
import { CenteredContainerComponent } from '../../atomic-design/centered-container/centered-container.component';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { api } from '../http.service';
import { Credentials } from '../types';
import { Router, RouterLink } from '@angular/router';
import { PATH_LOGIN, route } from '../app.routes';

@Component({
  selector: 'ins-register',
  imports: [
    ReactiveFormsModule,
    InputComponent,
    CenteredContainerComponent,
    ButtonComponent,
    RouterLink,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent extends DefaultComponent {
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

  protected readonly route = route;
  protected readonly PATH_LOGIN = PATH_LOGIN;

  register() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set(undefined);
      return;
    }
    this.httpService
      .sweetFetch<Credentials, Credentials>(
        api('users'),
        'POST',
        this.form.getRawValue(),
      )
      .then((savedUser) => {
        this.errorMessage.set(undefined);
        this.router.navigate([PATH_LOGIN]);
      })
      .catch((reason) => {
        this.errorMessage.set("Une erreur s'est produite.");
      });
  }
}
