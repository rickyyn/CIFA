// src/lib/mockData.ts

export interface Student {
    id: string;
    name: string;
    ra: string; // Registro Acadêmico
    course: string;
    period: 'Manhã' | 'Tarde' | 'Noite';
    cycle: number;
    status: 'Ativo' | 'Inativo' | 'Trancado';
    avatarUrl?: string;
}

export interface AccessLog {
    id: string;
    studentId: string;
    timestamp: string; // Formato ISO para facilitar formatação de datas no front-end
    status: 'Autorizado' | 'Negado';
    gate: string; // Identificação da Catraca
    reason?: string; // Motivo em caso de acesso negado
}

export const mockStudents: Student[] = [
    {
        id: '1',
        name: 'Ana Luiza Silva',
        ra: '11122233344',
        course: 'Desenvolvimento de Software Multiplataforma',
        period: 'Manhã',
        cycle: 3,
        status: 'Ativo',
    },
    {
        id: '2',
        name: 'Bruno Costa',
        ra: '55566677788',
        course: 'Desenvolvimento de Software Multiplataforma',
        period: 'Noite',
        cycle: 1,
        status: 'Ativo',
    },
    {
        id: '3',
        name: 'Carla Dias',
        ra: '99900011122',
        course: 'Análise e Desenvolvimento de Sistemas',
        period: 'Tarde',
        cycle: 5,
        status: 'Trancado',
    },
    {
        id: '4',
        name: 'Diego Fernandes',
        ra: '33344455566',
        course: 'Gestão Empresarial',
        period: 'Manhã',
        cycle: 2,
        status: 'Ativo',
    },
    {
        id: '5',
        name: 'Elena Rodrigues',
        ra: '77788899900',
        course: 'Desenvolvimento de Software Multiplataforma',
        period: 'Noite',
        cycle: 6,
        status: 'Inativo',
    }
];

export const mockAccessLogs: AccessLog[] = [
    {
        id: 'log-1',
        studentId: '1', // Refere-se à Ana Luiza
        timestamp: '2026-03-28T07:45:00Z',
        status: 'Autorizado',
        gate: 'Catraca Principal - Entrada',
    },
    {
        id: 'log-2',
        studentId: '2', // Refere-se ao Bruno
        timestamp: '2026-03-28T18:50:00Z',
        status: 'Autorizado',
        gate: 'Catraca Principal - Entrada',
    },
    {
        id: 'log-3',
        studentId: '3', // Refere-se à Carla
        timestamp: '2026-03-28T13:15:00Z',
        status: 'Negado',
        gate: 'Catraca Lateral',
        reason: 'Matrícula Trancada',
    },
    {
        id: 'log-4',
        studentId: '4', // Refere-se ao Diego
        timestamp: '2026-03-28T08:00:00Z',
        status: 'Autorizado',
        gate: 'Catraca Principal - Entrada',
    },
    {
        id: 'log-5',
        studentId: '5', // Refere-se à Elena
        timestamp: '2026-03-27T19:05:00Z',
        status: 'Negado',
        gate: 'Catraca Principal - Entrada',
        reason: 'Aluno Inativo',
    },
    {
        id: 'log-6',
        studentId: '1', // Refere-se à Ana Luiza (Dia anterior)
        timestamp: '2026-03-27T07:42:00Z',
        status: 'Autorizado',
        gate: 'Catraca Principal - Entrada',
    },
    {
        id: 'log-7',
        studentId: '2', // Refere-se ao Bruno (Dia anterior)
        timestamp: '2026-03-26T18:55:00Z',
        status: 'Autorizado',
        gate: 'Catraca Estacionamento',
    }
];