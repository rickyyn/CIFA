// src/app/(hub)/estudantes/page.tsx

'use client';

import { useState, useMemo, Suspense, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';

// ==========================================
// Ícones em SVG
// ==========================================
const SearchIcon = () => (
  <svg className="w-5 h-5 text-indigo-950/50 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
  </svg>
);

const FilterIcon = () => (
  <svg className="w-5 h-5 text-indigo-950 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 01-.659 1.591l-5.432 5.432a2.25 2.25 0 00-.659 1.591v2.927a2.25 2.25 0 01-1.244 2.013L9.75 21v-6.568a2.25 2.25 0 00-.659-1.591L3.659 7.409A2.25 2.25 0 013 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0112 3z" />
  </svg>
);

const EditIcon = () => (
  <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L6.832 19.82a4.5 4.5 0 01-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 011.13-1.897L16.863 4.487zm0 0L19.5 7.125" />
  </svg>
);

const CloseIcon = () => (
  <svg className="w-5 h-5 text-indigo-950 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
  </svg>
);

const ChevronLeftIcon = () => (
  <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
  </svg>
);

const DownloadIcon = () => (
  <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
  </svg>
);

// ==========================================
// Dados Iniciais (Mock)
// ==========================================
interface Student {
  id: string;
  name: string;
  age: number;
  course: string;
  cycle: number;
  period: string;
  ra: string;
  contact: string;
  avatarUrl: string;
}

const initialMockStudents: Student[] = [
  { id: '1', name: 'Leonardo Mendonça', age: 22, course: 'Desenvolvimento de Software Multiplataforma', cycle: 3, period: 'Manhã', ra: '11122233344', contact: '(13) 99123-4567', avatarUrl: 'https://i.pravatar.cc/150?u=leonardo' },
  { id: '2', name: 'Beatriz Almeida', age: 20, course: 'Análise e Desenvolvimento de Sistemas', cycle: 1, period: 'Noite', ra: '55566677788', contact: '(13) 99876-5432', avatarUrl: 'https://i.pravatar.cc/150?u=beatriz' },
  { id: '3', name: 'Carlos Eduardo Silva', age: 25, course: 'Gestão Empresarial', cycle: 5, period: 'Tarde', ra: '99900011122', contact: '(11) 98888-7777', avatarUrl: 'https://i.pravatar.cc/150?u=carlos' },
  { id: '4', name: 'Mariana Costa', age: 19, course: 'Comércio Exterior', cycle: 2, period: 'Manhã', ra: '33344455566', contact: '(13) 97777-6666', avatarUrl: 'https://i.pravatar.cc/150?u=mariana' },
  { id: '5', name: 'Felipe Rocha', age: 28, course: 'Desenvolvimento de Software Multiplataforma', cycle: 6, period: 'Noite', ra: '77788899900', contact: '(11) 96666-5555', avatarUrl: 'https://i.pravatar.cc/150?u=felipe' },
  { id: '6', name: 'Juliana Castro', age: 21, course: 'Análise e Desenvolvimento de Sistemas', cycle: 4, period: 'Manhã', ra: '12312312345', contact: '(13) 95555-4444', avatarUrl: 'https://i.pravatar.cc/150?u=juliana' },
  { id: '7', name: 'Thiago Oliveira', age: 24, course: 'Gestão Empresarial', cycle: 3, period: 'Noite', ra: '98765432100', contact: '(13) 94444-3333', avatarUrl: 'https://i.pravatar.cc/150?u=thiago' },
];

function EstudantesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const urlStudentId = searchParams.get('id');

  const [students, setStudents] = useState<Student[]>(initialMockStudents);
  const [searchQuery, setSearchQuery] = useState('');
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editFormData, setEditFormData] = useState<Student | null>(null);

  useEffect(() => {
    const savedData = localStorage.getItem('cifa_students_data');
    if (savedData) {
      try { setStudents(JSON.parse(savedData)); } 
      catch (e) { console.error(e); }
    }
  }, []);

  const [selectedCourses, setSelectedCourses] = useState<string[]>([]);
  const [selectedPeriods, setSelectedPeriods] = useState<string[]>([]);
  const [selectedCycles, setSelectedCycles] = useState<number[]>([]);

  const COURSES = Array.from(new Set(initialMockStudents.map(s => s.course)));
  const PERIODS = Array.from(new Set(initialMockStudents.map(s => s.period)));
  const CYCLES = [1, 2, 3, 4, 5, 6];

  const selectedStudent = useMemo(() => {
    const id = urlStudentId || (students.length > 0 ? students[0].id : null);
    return students.find(s => s.id === id) || null;
  }, [urlStudentId, students]);

  useEffect(() => {
    setIsEditing(false);
    setEditFormData(null);
  }, [urlStudentId]);

  const filteredStudents = useMemo(() => {
    return students.filter(student => {
      const matchesSearch = student.name.toLowerCase().includes(searchQuery.toLowerCase()) || student.ra.includes(searchQuery);
      const matchesCourse = selectedCourses.length === 0 || selectedCourses.includes(student.course);
      const matchesPeriod = selectedPeriods.length === 0 || selectedPeriods.includes(student.period);
      const matchesCycle = selectedCycles.length === 0 || selectedCycles.includes(student.cycle);
      return matchesSearch && matchesCourse && matchesPeriod && matchesCycle;
    });
  }, [searchQuery, selectedCourses, selectedPeriods, selectedCycles, students]);

  const toggleFilter = (list: any[], setList: Function, value: any) => {
    setList(list.includes(value) ? list.filter((item: any) => item !== value) : [...list, value]);
  };

  const handleSaveEdit = () => {
    if (editFormData) {
      const updatedStudents = students.map(s => s.id === editFormData.id ? editFormData : s);
      setStudents(updatedStudents);
      localStorage.setItem('cifa_students_data', JSON.stringify(updatedStudents));
      setIsEditing(false);
      setEditFormData(null);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setEditFormData(prev => prev ? { ...prev, [name]: name === 'age' || name === 'cycle' ? Number(value) : value } : null);
  };

  const handleStartEdit = () => {
    if (selectedStudent) {
      setEditFormData({ ...selectedStudent });
      setIsEditing(true);
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditFormData(null);
  };

  const [isExporting, setIsExporting] = useState(false);
  const handleExport = () => {
    setIsExporting(true);
    setTimeout(() => {
      const headers = ['Nome', 'RA', 'Curso', 'Período', 'Ciclo', 'Idade', 'Contato'];
      const csvRows = filteredStudents.map(s => `"${s.name}","${s.ra}","${s.course}","${s.period}",${s.cycle},${s.age},"${s.contact}"`);
      const csvContent = [headers.join(','), ...csvRows].join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', 'relatorio_estudantes_cifa.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setIsExporting(false);
    }, 600);
  };

  const hasActiveFilters = selectedCourses.length > 0 || selectedPeriods.length > 0 || selectedCycles.length > 0;

  return (
    <>
      <header className="flex flex-col gap-4 flex-shrink-0 z-10">
        <div className="flex items-center text-indigo-950/50 hover:text-indigo-950 transition-colors cursor-pointer w-max">
          <ChevronLeftIcon />
          <span className="font-poppins text-base font-semibold ml-1">Alunos</span>
        </div>

        <div className="flex items-center justify-between w-full">
          <div className="flex items-center gap-3 w-full max-w-xl relative">
            <div className="relative flex-1">
              <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none"><SearchIcon /></div>
              <input type="text" placeholder="Pesquisar por nome ou RA..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="w-full pl-11 pr-4 py-2.5 bg-white border border-gray-200 rounded-xl font-poppins text-sm text-indigo-950 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all shadow-sm" />
            </div>
            <button 
              onClick={() => setIsFilterModalOpen(true)}
              className={`flex items-center gap-2 px-5 py-2.5 bg-white border rounded-xl font-poppins text-sm font-semibold transition-all shadow-sm ${hasActiveFilters ? 'border-indigo-500 text-indigo-700 bg-indigo-50/50' : 'border-gray-200 text-indigo-950 hover:bg-gray-50'}`}
            >
              <FilterIcon /> Filtros {hasActiveFilters && `(Ativos)`}
            </button>
          </div>
          
          <button onClick={handleExport} disabled={isExporting || filteredStudents.length === 0} className={`flex items-center gap-2 px-6 py-2.5 bg-white border border-gray-200 rounded-xl font-poppins text-sm font-semibold transition-all shadow-sm ${isExporting ? 'opacity-70 cursor-wait' : 'hover:bg-gray-50 text-indigo-950'} ${filteredStudents.length === 0 ? 'opacity-50 cursor-not-allowed' : ''}`}>
            {isExporting ? <span className="animate-pulse">Exportando...</span> : <><DownloadIcon /> Exportar CSV</>}
          </button>
        </div>
      </header>

      <section className="flex-1 flex gap-8 min-h-0 z-0 mt-4">
        
        {/* LADO ESQUERDO: LISTA */}
        <div className="w-[340px] flex flex-col flex-shrink-0 h-full">
          <div className="pb-2 border-b border-gray-200 mb-2 flex justify-between items-end">
            <h3 className="font-poppins text-sm font-semibold text-indigo-950">Nome</h3>
            <span className="font-poppins text-[10px] font-medium text-indigo-950/50">{filteredStudents.length} {filteredStudents.length === 1 ? 'resultado' : 'resultados'}</span>
          </div>

          <div className="flex-1 overflow-y-auto pr-1 custom-scrollbar flex flex-col gap-1">
            {filteredStudents.length > 0 ? (
              filteredStudents.map((student, index) => {
                const isSelected = selectedStudent?.id === student.id;
                return (
                  <div 
                    key={student.id} 
                    onClick={() => router.replace(`/estudantes?id=${student.id}`, { scroll: false })} 
                    style={{ animationDelay: `${index * 30}ms` }} 
                    className={`flex items-center justify-between p-2.5 rounded-xl cursor-pointer transition-all duration-200 animate-slide-in opacity-0 fill-mode-forwards ${
                      isSelected ? 'bg-indigo-50/80 shadow-sm border border-indigo-100 scale-[1.02]' : 'hover:bg-gray-50 border border-transparent hover:scale-[1.01]'
                    }`}
                  >
                    <div className="flex items-center gap-3 overflow-hidden">
                      <img src={student.avatarUrl} alt={student.name} className="w-10 h-10 rounded-full object-cover border-[1.5px] border-white shadow-sm flex-shrink-0" />
                      <span className={`font-poppins text-sm truncate ${isSelected ? 'font-bold text-indigo-950' : 'font-medium text-indigo-950/80'}`}>{student.name}</span>
                    </div>
                    <button className={`px-4 py-1.5 rounded-lg font-poppins text-xs font-semibold transition-all flex-shrink-0 ml-2 ${isSelected ? 'bg-indigo-950 text-white shadow-md' : 'bg-indigo-950/90 text-white hover:bg-indigo-950'}`}>Acessar</button>
                  </div>
                );
              })
            ) : (
              <div className="flex flex-col items-center justify-center h-full gap-2 text-indigo-950/40 animate-fade-in"><SearchIcon /><span className="font-poppins text-xs font-medium">Nenhum aluno encontrado.</span></div>
            )}
          </div>
        </div>

        {/* LADO DIREITO: PRONTUÁRIO */}
        <div className="flex-1 bg-[#eef0f4] rounded-[1.5rem] p-8 flex flex-col h-full overflow-hidden shadow-inner relative">
          {selectedStudent ? (
            <div key={selectedStudent.id} className="flex flex-col h-full animate-fade-in">
              
              {/* CABEÇALHO DO PRONTUÁRIO OTIMIZADO E BLINDADO */}
              <div className="flex items-start justify-between mb-8 flex-shrink-0 w-full gap-4">
                
                <div className="flex items-center gap-5 min-w-0 flex-1">
                  <img src={selectedStudent.avatarUrl} alt={selectedStudent.name} className="w-20 h-20 rounded-full object-cover shadow-md border-[3px] border-white flex-shrink-0" />
                  {/* min-w-0 no pai e truncate no h2 garantem que o texto longo vai cortar em vez de esmagar os botões */}
                  <h2 className="font-poppins text-2xl font-bold text-indigo-950 tracking-tight leading-tight truncate">
                    {selectedStudent.name}
                  </h2>
                </div>
                
                {/* CONTAINER DE BOTÕES RIGOROSAMENTE ALINHADOS */}
                <div className="flex items-center gap-2 flex-shrink-0">
                  {!isEditing ? (
                    <button 
                      onClick={handleStartEdit}
                      className="flex items-center gap-2 bg-indigo-950 text-white px-5 py-2.5 rounded-xl font-poppins text-sm font-semibold hover:bg-indigo-800 transition-all shadow-sm"
                    >
                      <EditIcon /> Editar
                    </button>
                  ) : (
                    <>
                      <button 
                        onClick={handleCancelEdit} 
                        className="px-4 py-2.5 rounded-xl font-poppins text-sm font-bold text-gray-500 hover:bg-gray-200 transition-all"
                      >
                        Cancelar
                      </button>
                      <button 
                        onClick={handleSaveEdit} 
                        className="bg-emerald-600 text-white px-5 py-2.5 rounded-xl font-poppins text-sm font-bold hover:bg-emerald-700 transition-all shadow-md"
                      >
                        Salvar
                      </button>
                    </>
                  )}
                </div>

              </div>

              <div className="grid grid-cols-2 gap-x-8 gap-y-6 overflow-y-auto custom-scrollbar pr-2 pb-4">
                {[
                  { label: 'Nome Completo', name: 'name', value: isEditing ? editFormData?.name : selectedStudent.name },
                  { label: 'Matrícula (RA)', name: 'ra', value: isEditing ? editFormData?.ra : selectedStudent.ra },
                  { label: 'Curso', name: 'course', type: 'select', options: COURSES, value: isEditing ? editFormData?.course : selectedStudent.course },
                  { label: 'Período', name: 'period', type: 'select', options: PERIODS, value: isEditing ? editFormData?.period : selectedStudent.period },
                  { label: 'Ciclo Atual', name: 'cycle', type: 'number', value: isEditing ? editFormData?.cycle : selectedStudent.cycle },
                  { label: 'Idade', name: 'age', type: 'number', value: isEditing ? editFormData?.age : selectedStudent.age },
                  { label: 'Contato', name: 'contact', value: isEditing ? editFormData?.contact : selectedStudent.contact },
                ].map((field) => (
                  <div key={field.name} className="flex flex-col gap-1.5 group min-w-0">
                    <span className="font-poppins text-indigo-950/60 text-xs font-semibold uppercase tracking-wider">{field.label}</span>
                    {isEditing ? (
                      field.type === 'select' ? (
                        <select 
                          name={field.name} 
                          value={(field.value as string) ?? ''} 
                          onChange={handleInputChange}
                          className="w-full bg-white border border-gray-200 rounded-lg px-3 py-2.5 font-poppins text-sm font-semibold text-indigo-950 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all shadow-sm"
                        >
                          {field.options?.map(opt => <option key={opt} value={opt}>{opt}</option>)}
                        </select>
                      ) : (
                        <input 
                          name={field.name}
                          type={field.type || 'text'}
                          value={field.value ?? ''}
                          onChange={handleInputChange}
                          className="w-full bg-white border border-gray-200 rounded-lg px-3 py-2.5 font-poppins text-sm font-semibold text-indigo-950 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all shadow-sm"
                        />
                      )
                    ) : (
                      <span className="font-poppins text-indigo-950 text-base font-bold leading-tight truncate">
                        {field.name === 'cycle' ? `${field.value}º Semestre` : field.name === 'age' ? `${field.value} anos` : field.value}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-indigo-950/30"><span className="font-poppins text-sm font-medium">Selecione um aluno para ver o prontuário.</span></div>
          )}
        </div>
      </section>

      {/* MODAL DE FILTROS */}
      {isFilterModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-indigo-950/20 backdrop-blur-sm animate-fade-in" onClick={() => setIsFilterModalOpen(false)} />
          <div className="relative w-full max-w-[500px] bg-white rounded-3xl shadow-2xl flex flex-col animate-scale-in">
            <div className="flex items-center justify-between p-6 border-b border-gray-100 flex-shrink-0">
              <h3 className="font-poppins text-lg font-bold text-indigo-950 flex items-center gap-2"><FilterIcon /> Filtros Avançados</h3>
              <button onClick={() => setIsFilterModalOpen(false)} className="p-2 hover:bg-gray-100 rounded-full transition-colors text-indigo-950/50 hover:text-indigo-950"><CloseIcon /></button>
            </div>
            
            <div className="p-6 flex flex-col gap-6 max-h-[60vh] overflow-y-auto custom-scrollbar">
              <div>
                <h4 className="font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-3">Ciclo (Semestre)</h4>
                <div className="flex flex-wrap gap-2">
                  {CYCLES.map(c => (
                    <button key={c} onClick={() => toggleFilter(selectedCycles, setSelectedCycles, c)} className={`px-4 py-2 rounded-xl font-poppins text-xs font-semibold border transition-all ${selectedCycles.includes(c) ? 'bg-indigo-950 text-white border-indigo-950' : 'bg-white text-indigo-950 border-gray-200 hover:border-indigo-300'}`}>
                      {c}º
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <h4 className="font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-3">Cursos</h4>
                <div className="flex flex-col gap-2">
                  {COURSES.map(course => (
                    <label key={course} className="flex items-center gap-3 cursor-pointer group">
                      <input type="checkbox" checked={selectedCourses.includes(course)} onChange={() => toggleFilter(selectedCourses, setSelectedCourses, course)} className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer border-gray-300" />
                      <span className="font-poppins text-sm text-indigo-950 font-medium group-hover:text-indigo-600 transition-colors">{course}</span>
                    </label>
                  ))}
                </div>
              </div>
              <div>
                <h4 className="font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-3">Turnos</h4>
                <div className="flex flex-col gap-2">
                  {PERIODS.map(period => (
                    <label key={period} className="flex items-center gap-3 cursor-pointer group">
                      <input type="checkbox" checked={selectedPeriods.includes(period)} onChange={() => toggleFilter(selectedPeriods, setSelectedPeriods, period)} className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer border-gray-300" />
                      <span className="font-poppins text-sm text-indigo-950 font-medium group-hover:text-indigo-600 transition-colors">{period}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between p-6 border-t border-gray-100 bg-gray-50 rounded-b-3xl flex-shrink-0">
              <button onClick={() => { setSelectedCourses([]); setSelectedPeriods([]); setSelectedCycles([]); }} className={`font-poppins text-sm font-semibold transition-colors ${hasActiveFilters ? 'text-red-600 hover:text-red-700' : 'text-gray-400 cursor-not-allowed'}`} disabled={!hasActiveFilters}>Limpar Filtros</button>
              <button onClick={() => setIsFilterModalOpen(false)} className="bg-indigo-950 text-white px-8 py-3 rounded-xl font-poppins text-sm font-bold hover:bg-indigo-900 transition-all shadow-sm">Aplicar e Fechar</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default function EstudantesPage() {
  return (
    <div className="h-full w-full p-6 flex flex-col gap-6 overflow-hidden bg-[#f8f9fc] rounded-[2rem] relative">
      <Suspense fallback={<div className="h-full w-full flex items-center justify-center font-poppins text-indigo-950/40">Carregando dados...</div>}>
        <EstudantesContent />
      </Suspense>

      <style dangerouslySetInnerHTML={{__html: `
        .custom-scrollbar::-webkit-scrollbar { width: 6px; }
        .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
        .fill-mode-forwards { animation-fill-mode: forwards; }
        .animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
        .animate-slide-in { animation: slideIn 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
        .animate-scale-in { animation: scaleIn 0.15s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes slideIn { from { opacity: 0; transform: translateX(-10px); } to { opacity: 1; transform: translateX(0); } }
        @keyframes scaleIn { from { opacity: 0; transform: scale(0.96) translateY(-10px); } to { opacity: 1; transform: scale(1) translateY(0); } }
      `}} />
    </div>
  );
}