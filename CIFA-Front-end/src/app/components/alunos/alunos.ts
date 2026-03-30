import { Component, inject, OnInit } from '@angular/core';
import { AlunoService } from '../../services/aluno-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alunos',
  imports: [CommonModule],
  templateUrl: './alunos.html',
  styleUrl: './alunos.css',
})
export class Alunos {

  private alunoService = inject(AlunoService);
  public alunos: any = [];

  ngOnInit(){
    this.exibirAlunos()
  }
  exibirAlunos(){
    this.alunoService.getAlunos().subscribe({
      next: (res) => {
        console.log('Sucesso:', res)
        this.alunos = res
      },
      error: (err) => console.error('Erro:', err)
    });
  }

}
