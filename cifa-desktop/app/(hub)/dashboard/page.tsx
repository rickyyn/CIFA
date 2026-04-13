// src/app/(hub)/dashboard/page.tsx

'use client';

import { useState } from 'react';

// Componentes Icon Badge para os KPIs com flex-shrink-0 garantido
const KpiIcon = ({ path }: { path: string }) => (
    <svg className="w-7 h-7 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
        <path d={path} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
);

// Dados do Gráfico de Barras
const weeklyData = [
    { day: 'Seg', value: 25 },
    { day: 'Ter', value: 45 },
    { day: 'Qua', value: 27 },
    { day: 'Qui', value: 38 },
    { day: 'Sex', value: 45 },
    { day: 'Sáb', value: 12 },
    { day: 'Dom', value: 10 },
];

// Dados do Gráfico de Rosca (Proporção de Cursos)
const courseDistribution = [
    { course: 'ADS', percentage: 22.8, color: '#f43f5e' },
    { course: 'COMEX', percentage: 14.2, color: '#f97316' },
    { course: 'DSM', percentage: 24.7, color: '#eab308' },
    { course: 'GE', percentage: 16.8, color: '#22c55e' },
    { course: 'PQ', percentage: 21.5, color: '#0ea5e9' },
];

export default function DashboardPage() {
    const maxWeeklyValue = Math.max(...weeklyData.map(d => d.value));

    // Estado para o hover do gráfico de rosca
    const [hoveredCourse, setHoveredCourse] = useState<{ course: string; percentage: number } | null>(null);

    // Variável para acumular o preenchimento do SVG do donut chart
    let cumulativePercent = 0;

    return (
        // Contêiner base ocupando 100% com paddings otimizados
        <div className="h-full w-full p-6 flex flex-col gap-5 overflow-hidden">

            {/* Cabeçalho */}
            <header className="flex items-start justify-between flex-shrink-0">
                <div className="flex flex-col gap-0.5">
                    <h1 className="text-indigo-950 text-4xl font-extrabold tracking-tight">
                        Olá, Vinicius!
                    </h1>
                    <p className="text-indigo-950/60 text-sm font-medium">
                        28 de março de 2026
                    </p>
                    <p className="text-indigo-950/40 text-[11px] font-semibold mt-2 uppercase tracking-wider">
                        Dashboard &gt;
                    </p>
                </div>
            </header>

            {/* Grid de KPIs Superiores - Altura travada de forma segura */}
            <section className="grid grid-cols-1 md:grid-cols-3 gap-5 flex-shrink-0 h-[130px]">

                {/* KPI: Total de Acessos (Ícone Corrigido) */}
                <div className="bg-indigo-950 rounded-[1.25rem] p-5 flex flex-col justify-between text-white overflow-hidden">
                    <div className="text-white/80">
                        <KpiIcon path="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75" />
                    </div>
                    <div>
                        <span className="block text-3xl font-bold leading-none mb-1">1.432</span>
                        <h2 className="text-white/80 text-[13px] font-medium leading-tight">Total de acessos</h2>
                    </div>
                </div>

                {/* KPI: Acessos Autorizados */}
                <div className="bg-indigo-950 rounded-[1.25rem] p-5 flex flex-col justify-between text-white overflow-hidden">
                    <div className="text-white/80">
                        <KpiIcon path="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M12 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0z M17 11l2 2 4-4" />
                    </div>
                    <div>
                        <span className="block text-3xl font-bold leading-none mb-1">1.390</span>
                        <h2 className="text-white/80 text-[13px] font-medium leading-tight">Acessos autorizados</h2>
                    </div>
                </div>

                {/* KPI: Acessos Negados */}
                <div className="bg-[#ff3b4b] rounded-[1.25rem] p-5 flex flex-col justify-between text-white shadow-lg shadow-red-500/20 overflow-hidden">
                    <div className="text-white/90">
                        <KpiIcon path="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M12 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0z M18 8l4 4 M22 8l-4 4" />
                    </div>
                    <div>
                        <span className="block text-3xl font-bold leading-none mb-1">42</span>
                        <h2 className="text-white/90 text-[13px] font-medium leading-tight">Acessos negados</h2>
                    </div>
                </div>

            </section>

            {/* Grid Inferior - Gráficos ocupando o resto do espaço */}
            <section className="grid grid-cols-1 lg:grid-cols-3 gap-5 flex-1 min-h-0">

                {/* Gráfico de Barras */}
                <div className="lg:col-span-2 bg-indigo-950 rounded-[1.25rem] p-6 flex flex-col h-full overflow-hidden">
                    <div className="flex items-center gap-2 mb-4 text-white flex-shrink-0">
                        <KpiIcon path="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2 M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2 M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2 m-6 9a2 2 0 1 0 0-4 2 2 0 0 0 0 4z" />
                        <h3 className="text-lg font-semibold tracking-wide">Acessos da semana</h3>
                    </div>

                    {/* Área principal das barras */}
                    <div className="flex-1 flex items-end justify-between px-2 gap-3 min-h-0 pt-4">
                        {weeklyData.map((item, index) => {
                            const heightPercentage = (item.value / maxWeeklyValue) * 100;
                            return (
                                <div key={index} className="flex flex-col items-center w-full h-full justify-end group">

                                    {/* Container da barra com altura segura */}
                                    <div className="flex-1 w-full flex flex-col justify-end items-center relative min-h-0">
                                        {/* Tooltip do Hover - Posicionado acima da barra para não quebrar layout */}
                                        <div className="absolute bottom-full mb-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200 z-10 pointer-events-none">
                      <span className="text-white text-[11px] font-semibold bg-white/15 px-2 py-0.5 rounded shadow-sm">
                        {item.value}
                      </span>
                                        </div>
                                        {/* Barra Visual */}
                                        <div
                                            className="w-full max-w-[36px] bg-white rounded-t-sm transition-all duration-300 hover:bg-indigo-300"
                                            style={{ height: `${heightPercentage}%`, minHeight: '6px' }}
                                        />
                                    </div>

                                    {/* Legenda do Eixo X - Espaço garantido no bottom */}
                                    <span className="text-white/60 text-[11px] font-semibold mt-2 h-[16px] flex items-center justify-center flex-shrink-0">
                    {item.day}
                  </span>
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* Gráfico de Rosca */}
                <div className="bg-indigo-950 rounded-[1.25rem] p-6 flex flex-col items-center h-full overflow-hidden">

                    <h3 className="w-full text-white text-lg font-semibold tracking-wide flex-shrink-0 text-left mb-2">
                        Proporção de Cursos
                    </h3>

                    {/* Container do Donut - Encolhe dinamicamente mas mantém aspecto quadrado */}
                    <div className="flex-1 w-full flex items-center justify-center min-h-0 relative">
                        <div className="w-full h-full max-w-[170px] max-h-[170px] relative flex items-center justify-center">
                            <svg viewBox="0 0 36 36" className="w-full h-full transform -rotate-90 overflow-visible">
                                {courseDistribution.map((item, index) => {
                                    const dashArray = `${item.percentage} ${100 - item.percentage}`;
                                    const dashOffset = 100 - cumulativePercent;
                                    cumulativePercent += item.percentage;

                                    return (
                                        <circle
                                            key={index}
                                            cx="18"
                                            cy="18"
                                            r="15.91549430918954"
                                            fill="transparent"
                                            stroke={item.color}
                                            strokeWidth="5.5"
                                            strokeDasharray={dashArray}
                                            strokeDashoffset={dashOffset}
                                            className="transition-all duration-300 cursor-pointer hover:stroke-[7px] origin-center"
                                            onMouseEnter={() => setHoveredCourse({ course: item.course, percentage: item.percentage })}
                                            onMouseLeave={() => setHoveredCourse(null)}
                                        />
                                    );
                                })}
                            </svg>

                            {/* Informação Central */}
                            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                                {hoveredCourse ? (
                                    <>
                                        <span className="text-white text-2xl font-bold leading-none">{hoveredCourse.percentage}%</span>
                                        <span className="text-white/70 text-[10px] font-bold tracking-wider mt-1 uppercase">{hoveredCourse.course}</span>
                                    </>
                                ) : (
                                    <span className="text-white/40 text-[10px] font-medium text-center px-4 leading-tight">
                    Hover para detalhes
                  </span>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Legenda (Inferior) - Espaço garantido */}
                    <div className="w-full flex flex-col gap-2 mt-4 flex-shrink-0">
                        {/* Dividindo em duas linhas para não estourar lateralmente */}
                        <div className="flex justify-between w-full px-2">
                            {courseDistribution.slice(0, 3).map((item, index) => (
                                <div key={index} className="flex items-center gap-1.5 cursor-pointer group" onMouseEnter={() => setHoveredCourse({ course: item.course, percentage: item.percentage })} onMouseLeave={() => setHoveredCourse(null)}>
                                    <div className="w-2.5 h-2.5 rounded-full flex-shrink-0 transition-transform group-hover:scale-125" style={{ backgroundColor: item.color }} />
                                    <span className="text-white/70 text-[10px] font-semibold group-hover:text-white truncate">{item.course}</span>
                                </div>
                            ))}
                        </div>
                        <div className="flex justify-around w-full px-4">
                            {courseDistribution.slice(3, 5).map((item, index) => (
                                <div key={index} className="flex items-center gap-1.5 cursor-pointer group" onMouseEnter={() => setHoveredCourse({ course: item.course, percentage: item.percentage })} onMouseLeave={() => setHoveredCourse(null)}>
                                    <div className="w-2.5 h-2.5 rounded-full flex-shrink-0 transition-transform group-hover:scale-125" style={{ backgroundColor: item.color }} />
                                    <span className="text-white/70 text-[10px] font-semibold group-hover:text-white truncate">{item.course}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                </div>
            </section>

            {/* Rodapé */}
            <footer className="w-full text-center text-indigo-950/40 text-[11px] font-semibold uppercase tracking-widest flex-shrink-0 mt-1">
                CIFA © 2026
            </footer>

        </div>
    );
}