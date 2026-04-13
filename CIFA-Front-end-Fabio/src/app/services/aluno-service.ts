import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AlunoService {

  private http = inject(HttpClient)

  private readonly API = "https://jsonplaceholder.typicode.com";

  getAlunos(): Observable<any[]>{
    return this.http.get<any[]>(this.API + "/users");
  }

  postAlunos(aluno: any): Observable<any>{
    return this.http.post(this.API, aluno);
  }

  putAluno(id: number, aluno: any): Observable<any> {
    return this.http.put(`${this.API}/${id}`, aluno);
  }
  
  removeAlunos(id:number): Observable<any>{
    return this.http.delete(this.API +"/"+id);
  }


}
