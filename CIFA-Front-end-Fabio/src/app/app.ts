import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Alunos } from "./components/alunos/alunos";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Alunos],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('CIFA-Front-end');
}
