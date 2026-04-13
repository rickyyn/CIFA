// src/app/(hub)/logs/page.tsx

'use client';

import { useState, useMemo } from 'react';
import Link from 'next/link';

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

const ChevronLeftIcon = () => (
    <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
    </svg>
);

const ChevronRightIcon = () => (
    <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
    </svg>
);

const ChevronDownIcon = () => (
    <svg className="w-4 h-4 text-indigo-950 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
    </svg>
);

const DownloadIcon = () => (
    <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
    </svg>
);

const CalendarIcon = () => (
    <svg className="w-4 h-4 text-indigo-950/50 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
    </svg>
);

const CloseIcon = () => (
    <svg className="w-5 h-5 text-indigo-950 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
);

const ExternalLinkIcon = () => (
    <svg className="w-3.5 h-3.5 flex-shrink-0 opacity-0 group-hover/link:opacity-100 transition-opacity" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
    </svg>
);

// ==========================================
// Tipos e Dados Simulados Estratégicos
// ==========================================
interface AccessLog {
    id: string;
    studentId: string; // Adicionado para fazer a ponte com o Prontuário
    dateTime: string;
    userName: string;
    avatarUrl: string;
    period: string;
    course: string;
    isoDate: string;
}

const BASE_DATE = new Date(2026, 2, 28);
const formatMockDate = (d: Date) => `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
const toISODate = (d: Date) => d.toISOString().split('T')[0];

const generateMockLogs = (): AccessLog[] => {
    const logs: AccessLog[] = [];
    // Conectado com os IDs reais da página de Estudantes
    const users = [
        { id: '1', name: 'Leonardo Mendonça', img: 'https://i.pravatar.cc/150?u=leonardo' },
        { id: '2', name: 'Beatriz Almeida', img: 'https://i.pravatar.cc/150?u=beatriz' },
        { id: '3', name: 'Carlos Eduardo Silva', img: 'https://i.pravatar.cc/150?u=carlos' },
        { id: '4', name: 'Mariana Costa', img: 'https://i.pravatar.cc/150?u=mariana' }
    ];
    const courses = ['DSM', 'ADS', 'GE', 'COMEX'];
    const periods = ['Matutino', 'Vespertino', 'Noturno'];

    let logIdCounter = 1;

    const injectLogs = (targetDate: Date, count: number) => {
        for (let i = 0; i < count; i++) {
            const user = users[(logIdCounter + i) % users.length];
            const course = courses[(logIdCounter + i) % courses.length];
            const period = periods[(logIdCounter + i) % periods.length];

            logs.push({
                id: `log-${logIdCounter++}`,
                studentId: user.id,
                dateTime: `${formatMockDate(targetDate)} - 0${8 + (i % 4)}:${15 + (i * 10)}`,
                isoDate: toISODate(targetDate),
                userName: user.name,
                avatarUrl: user.img,
                period: period,
                course: course
            });
        }
    };

    injectLogs(new Date(2026, 2, 28), 8);
    injectLogs(new Date(2026, 2, 27), 5);
    injectLogs(new Date(2026, 2, 24), 7);
    injectLogs(new Date(2026, 1, 15), 10);
    injectLogs(new Date(2026, 1, 20), 8);
    injectLogs(new Date(2026, 0, 10), 7);
    injectLogs(new Date(2018, 5, 12), 3);

    return logs.sort((a, b) => new Date(b.isoDate).getTime() - new Date(a.isoDate).getTime());
};

const mockLogs = generateMockLogs();
const COURSES = Array.from(new Set(mockLogs.map(l => l.course)));
const PERIODS = Array.from(new Set(mockLogs.map(l => l.period)));

type DateFilterType = 'all' | 'daily' | 'weekly' | 'monthly' | 'custom';
const YEARS = Array.from({ length: (BASE_DATE.getFullYear() + 5) - 2010 + 1 }, (_, i) => 2010 + i);

export default function LogsPage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);

    const [selectedCourses, setSelectedCourses] = useState<string[]>([]);
    const [selectedPeriods, setSelectedPeriods] = useState<string[]>([]);
    const [dateFilter, setDateFilter] = useState<DateFilterType>('all');

    const [customStartDate, setCustomStartDate] = useState<Date | null>(null);
    const [customEndDate, setCustomEndDate] = useState<Date | null>(null);
    const [calendarViewDate, setCalendarViewDate] = useState<Date>(BASE_DATE);

    // Estados dos Dropdowns Customizados
    const [isMonthSelectOpen, setIsMonthSelectOpen] = useState(false);
    const [isYearSelectOpen, setIsYearSelectOpen] = useState(false);

    const [isExporting, setIsExporting] = useState(false);

    // ==========================================
    // LÓGICA DO CALENDÁRIO
    // ==========================================
    const months = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    const weekDays = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];

    const getDaysInMonth = (year: number, month: number) => new Date(year, month + 1, 0).getDate();
    const getFirstDayOfMonth = (year: number, month: number) => new Date(year, month, 1).getDay();

    const handlePrevMonth = (e: React.MouseEvent) => {
        e.stopPropagation();
        setCalendarViewDate(new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() - 1, 1));
    };

    const handleNextMonth = (e: React.MouseEvent) => {
        e.stopPropagation();
        setCalendarViewDate(new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() + 1, 1));
    };

    const selectMonth = (monthIndex: number) => {
        setCalendarViewDate(new Date(calendarViewDate.getFullYear(), monthIndex, 1));
        setIsMonthSelectOpen(false);
    };

    const selectYear = (year: number) => {
        setCalendarViewDate(new Date(year, calendarViewDate.getMonth(), 1));
        setIsYearSelectOpen(false);
    };

    const handleDateClick = (e: React.MouseEvent, day: number) => {
        e.stopPropagation();
        const clickedDate = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth(), day);

        if (!customStartDate || (customStartDate && customEndDate)) {
            setCustomStartDate(clickedDate);
            setCustomEndDate(null);
        } else if (clickedDate >= customStartDate) {
            setCustomEndDate(clickedDate);
        } else {
            setCustomStartDate(clickedDate);
            setCustomEndDate(null);
        }
    };

    const isSelected = (day: number) => {
        const d = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth(), day);
        const time = d.getTime();
        if (customStartDate && !customEndDate) return time === customStartDate.getTime();
        if (customStartDate && customEndDate) return time >= customStartDate.getTime() && time <= customEndDate.getTime();
        return false;
    };

    const isRangeBound = (day: number) => {
        const d = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth(), day);
        const time = d.getTime();
        return (customStartDate && time === customStartDate.getTime()) || (customEndDate && time === customEndDate.getTime());
    };

    const renderCalendar = () => {
        const year = calendarViewDate.getFullYear();
        const month = calendarViewDate.getMonth();
        const daysInMonth = getDaysInMonth(year, month);
        const firstDay = getFirstDayOfMonth(year, month);

        const days = [];
        for (let i = 0; i < firstDay; i++) {
            days.push(<div key={`empty-${i}`} className="w-8 h-8" />);
        }
        for (let i = 1; i <= daysInMonth; i++) {
            const selected = isSelected(i);
            const bound = isRangeBound(i);
            days.push(
                <button
                    key={`day-${i}`}
                    onClick={(e) => handleDateClick(e, i)}
                    className={`w-8 h-8 flex items-center justify-center rounded-full text-xs font-poppins font-medium transition-all
            ${bound ? 'bg-indigo-600 text-white shadow-md' : ''}
            ${selected && !bound ? 'bg-indigo-100 text-indigo-900' : ''}
            ${!selected ? 'text-indigo-950 hover:bg-gray-100' : ''}
          `}
                >
                    {i}
                </button>
            );
        }
        return days;
    };

    // ==========================================
    // MOTOR DE FILTRAGEM E EXPORTAÇÃO
    // ==========================================
    const filteredLogs = useMemo(() => {
        const todayISO = toISODate(BASE_DATE);
        const lastWeekDate = new Date(BASE_DATE);
        lastWeekDate.setDate(lastWeekDate.getDate() - 7);
        const lastWeekISO = toISODate(lastWeekDate);
        const firstDayOfMonthISO = toISODate(new Date(BASE_DATE.getFullYear(), BASE_DATE.getMonth(), 1));

        return mockLogs.filter(log => {
            const matchesSearch = log.userName.toLowerCase().includes(searchQuery.toLowerCase()) || log.course.toLowerCase().includes(searchQuery.toLowerCase());
            const matchesCourse = selectedCourses.length === 0 || selectedCourses.includes(log.course);
            const matchesPeriod = selectedPeriods.length === 0 || selectedPeriods.includes(log.period);

            let matchesDate = true;
            if (dateFilter === 'daily') matchesDate = log.isoDate === todayISO;
            else if (dateFilter === 'weekly') matchesDate = log.isoDate >= lastWeekISO && log.isoDate <= todayISO;
            else if (dateFilter === 'monthly') matchesDate = log.isoDate >= firstDayOfMonthISO && log.isoDate <= todayISO;
            else if (dateFilter === 'custom') {
                if (customStartDate && customEndDate) matchesDate = log.isoDate >= toISODate(customStartDate) && log.isoDate <= toISODate(customEndDate);
                else if (customStartDate) matchesDate = log.isoDate >= toISODate(customStartDate);
            }

            return matchesSearch && matchesCourse && matchesPeriod && matchesDate;
        });
    }, [searchQuery, selectedCourses, selectedPeriods, dateFilter, customStartDate, customEndDate]);

    const handleExport = () => {
        setIsExporting(true);
        setTimeout(() => {
            const headers = ['ID do Log', 'Data & Tempo', 'Usuário', 'Período', 'Curso'];
            const csvRows = filteredLogs.map(l => `"${l.id}","${l.dateTime}","${l.userName}","${l.period}","${l.course}"`);
            const csvContent = [headers.join(','), ...csvRows].join('\n');

            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.setAttribute('href', url);
            link.setAttribute('download', 'relatorio_logs_cifa.csv');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            setIsExporting(false);
        }, 600);
    };

    const toggleCourse = (course: string) => setSelectedCourses(prev => prev.includes(course) ? prev.filter(c => c !== course) : [...prev, course]);
    const togglePeriod = (period: string) => setSelectedPeriods(prev => prev.includes(period) ? prev.filter(p => p !== period) : [...prev, period]);
    const hasActiveFilters = selectedCourses.length > 0 || selectedPeriods.length > 0 || dateFilter !== 'all';

    return (
        <div className="h-full w-full p-6 flex flex-col gap-6 overflow-hidden bg-[#f8f9fc] rounded-[2rem] relative">

            <header className="flex flex-col gap-5 flex-shrink-0 z-10">
                <div className="flex items-center text-indigo-950/50 hover:text-indigo-950 transition-colors cursor-pointer w-max">
                    <ChevronLeftIcon />
                    <span className="font-poppins text-lg font-medium ml-1">Logs de Acesso</span>
                </div>

                <div className="flex items-center justify-between w-full">
                    <div className="flex items-center gap-4 w-full max-w-2xl">
                        <div className="relative flex-1">
                            <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none">
                                <SearchIcon />
                            </div>
                            <input
                                type="text"
                                placeholder="Pesquisar por usuário ou curso..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="w-full pl-12 pr-4 py-3 bg-white border border-gray-200 rounded-2xl font-poppins text-sm text-indigo-950 placeholder:text-indigo-950/40 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all shadow-sm"
                            />
                        </div>

                        <button
                            onClick={() => setIsFilterModalOpen(true)}
                            className={`flex items-center gap-2.5 px-6 py-3 bg-white border rounded-2xl font-poppins text-sm font-medium transition-all shadow-sm ${
                                hasActiveFilters
                                    ? 'border-indigo-500 text-indigo-700 bg-indigo-50/50'
                                    : 'border-gray-200 text-indigo-950 hover:bg-gray-50'
                            }`}
                        >
                            <FilterIcon />
                            Filtros Avançados {hasActiveFilters && `(Ativos)`}
                        </button>
                    </div>

                    <button
                        onClick={handleExport}
                        disabled={isExporting || filteredLogs.length === 0}
                        className={`flex items-center gap-2 px-8 py-3 bg-white border border-gray-200 rounded-2xl font-poppins text-sm font-medium transition-all shadow-sm
              ${isExporting ? 'opacity-70 cursor-wait' : 'hover:bg-gray-50 text-indigo-950'}
              ${filteredLogs.length === 0 ? 'opacity-50 cursor-not-allowed' : ''}
            `}
                    >
                        {isExporting ? <span className="animate-pulse">Exportando...</span> : <><DownloadIcon />Exportar CSV</>}
                    </button>
                </div>
            </header>

            {/* ================= ÁREA DA TABELA ================= */}
            <section className="flex-1 flex flex-col min-h-0 bg-white/40 rounded-2xl p-6 z-0 relative">
                <div className="absolute top-4 right-6 bg-white border border-gray-100 shadow-sm px-3 py-1 rounded-full text-xs font-semibold text-indigo-950/60 z-10">
                    {filteredLogs.length} {filteredLogs.length === 1 ? 'registro' : 'registros'} filtrados
                </div>

                <div className="grid grid-cols-12 gap-4 pb-4 border-b-2 border-gray-200 flex-shrink-0 px-2 mt-2">
                    <div className="col-span-3 flex items-center gap-2 cursor-pointer group">
                        <span className="font-poppins text-base font-medium text-indigo-950 group-hover:text-indigo-700 transition-colors">Data & Tempo</span>
                        <ChevronDownIcon />
                    </div>
                    <div className="col-span-4"><span className="font-poppins text-base font-medium text-indigo-950">Usuário</span></div>
                    <div className="col-span-2"><span className="font-poppins text-base font-medium text-indigo-950">Período</span></div>
                    <div className="col-span-3 text-left"><span className="font-poppins text-base font-medium text-indigo-950">Curso</span></div>
                </div>

                <div className="flex-1 overflow-y-auto custom-scrollbar flex flex-col pr-2 mt-2">
                    {filteredLogs.length > 0 ? (
                        filteredLogs.map((log, index) => (
                            <div
                                key={log.id}
                                style={{ animationDelay: `${index * 15}ms` }}
                                className="grid grid-cols-12 gap-4 items-center py-4 border-b border-gray-200/80 hover:bg-white/80 transition-all px-2 rounded-lg animate-slide-in opacity-0 fill-mode-forwards"
                            >
                                <div className="col-span-3"><span className="font-poppins text-sm text-indigo-950/70 font-semibold">{log.dateTime}</span></div>

                                {/* Aqui está o Cross-Linking Inteligente! */}
                                <div className="col-span-4 flex items-center gap-4 overflow-hidden">
                                    <Link href={`/estudantes?id=${log.studentId}`} className="flex items-center gap-4 group/link hover:bg-indigo-50 p-1.5 -ml-1.5 rounded-xl transition-colors">
                                        <img src={log.avatarUrl} alt={log.userName} className="w-9 h-9 rounded-full object-cover border-[1.5px] border-white shadow-sm flex-shrink-0 group-hover/link:border-indigo-200 transition-colors" />
                                        <span className="font-poppins text-sm text-indigo-950/90 font-medium truncate group-hover/link:text-indigo-700 transition-colors">{log.userName}</span>
                                        <ExternalLinkIcon />
                                    </Link>
                                </div>

                                <div className="col-span-2"><span className="font-poppins text-sm text-indigo-950/70 font-medium">{log.period}</span></div>
                                <div className="col-span-3 text-left truncate"><span className="font-poppins text-sm text-indigo-950/70 font-medium">{log.course}</span></div>
                            </div>
                        ))
                    ) : (
                        <div className="flex flex-col items-center justify-center h-full gap-2 text-indigo-950/40 animate-fade-in">
                            <CalendarIcon />
                            <span className="font-poppins text-sm font-medium">Nenhum log encontrado para o período ou filtros selecionados.</span>
                        </div>
                    )}
                </div>
            </section>

            {/* ================= MODAL DE FILTROS ================= */}
            {isFilterModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-indigo-950/20 backdrop-blur-sm animate-fade-in" onClick={() => setIsFilterModalOpen(false)} />

                    <div className="relative w-full max-w-[650px] bg-white rounded-3xl shadow-2xl flex flex-col animate-scale-in max-h-[90vh]">

                        <div className="flex items-center justify-between p-6 border-b border-gray-100 flex-shrink-0">
                            <h3 className="font-poppins text-lg font-bold text-indigo-950 flex items-center gap-2">
                                <FilterIcon /> Filtros Avançados
                            </h3>
                            <button onClick={() => setIsFilterModalOpen(false)} className="p-2 hover:bg-gray-100 rounded-full transition-colors text-indigo-950/50 hover:text-indigo-950"><CloseIcon /></button>
                        </div>

                        <div className="flex gap-8 p-6 overflow-y-auto custom-scrollbar">

                            <div className="w-[280px] flex-shrink-0">
                                <h4 className="flex items-center gap-2 font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-4"><CalendarIcon /> Período</h4>
                                <div className="grid grid-cols-2 gap-2 mb-4">
                                    <button onClick={() => setDateFilter('all')} className={`py-2 rounded-xl font-poppins text-xs font-semibold transition-colors border ${dateFilter === 'all' ? 'bg-indigo-950 text-white border-indigo-950' : 'bg-transparent text-indigo-950 hover:bg-gray-50 border-gray-200'}`}>Todo período</button>
                                    <button onClick={() => setDateFilter('daily')} className={`py-2 rounded-xl font-poppins text-xs font-semibold transition-colors border ${dateFilter === 'daily' ? 'bg-indigo-950 text-white border-indigo-950' : 'bg-transparent text-indigo-950 hover:bg-gray-50 border-gray-200'}`}>Diário</button>
                                    <button onClick={() => setDateFilter('weekly')} className={`py-2 rounded-xl font-poppins text-xs font-semibold transition-colors border ${dateFilter === 'weekly' ? 'bg-indigo-950 text-white border-indigo-950' : 'bg-transparent text-indigo-950 hover:bg-gray-50 border-gray-200'}`}>Semanal</button>
                                    <button onClick={() => setDateFilter('monthly')} className={`py-2 rounded-xl font-poppins text-xs font-semibold transition-colors border ${dateFilter === 'monthly' ? 'bg-indigo-950 text-white border-indigo-950' : 'bg-transparent text-indigo-950 hover:bg-gray-50 border-gray-200'}`}>Mensal</button>
                                    <button onClick={() => setDateFilter('custom')} className={`col-span-2 py-2 rounded-xl font-poppins text-xs font-semibold transition-colors border ${dateFilter === 'custom' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-transparent text-indigo-950 hover:bg-gray-50 border-gray-200'}`}>Customizado</button>
                                </div>

                                {dateFilter === 'custom' && (
                                    <div className="bg-gray-50 border border-gray-100 rounded-2xl p-4 animate-fade-in shadow-inner relative">

                                        {/* Header do Calendário com Dropdowns Customizados Premium */}
                                        <div className="flex items-center justify-between mb-4 relative z-10">
                                            <button onClick={handlePrevMonth} className="p-1.5 bg-white border border-gray-200 hover:border-indigo-400 rounded-lg shadow-sm transition-colors text-indigo-950"><ChevronLeftIcon /></button>

                                            <div className="flex gap-1.5 items-center">
                                                {/* Dropdown Customizado de Mês */}
                                                <div className="relative">
                                                    <button onClick={() => setIsMonthSelectOpen(!isMonthSelectOpen)} className="flex items-center gap-1 bg-white border border-gray-200 px-2.5 py-1.5 rounded-lg text-xs font-bold text-indigo-950 hover:border-indigo-400 transition-colors shadow-sm min-w-[90px] justify-between">
                                                        {months[calendarViewDate.getMonth()]}
                                                        <ChevronDownIcon />
                                                    </button>
                                                    {isMonthSelectOpen && (
                                                        <>
                                                            <div className="fixed inset-0 z-40" onClick={() => setIsMonthSelectOpen(false)} />
                                                            <div className="absolute top-full left-0 mt-1 w-32 bg-white border border-gray-100 rounded-xl shadow-xl z-50 p-1.5 max-h-48 overflow-y-auto custom-scrollbar animate-scale-in origin-top-left flex flex-col gap-0.5">
                                                                {months.map((m, i) => (
                                                                    <button key={m} onClick={() => selectMonth(i)} className={`text-left px-3 py-2 rounded-lg text-xs font-semibold transition-colors ${calendarViewDate.getMonth() === i ? 'bg-indigo-600 text-white' : 'text-indigo-950 hover:bg-indigo-50'}`}>{m}</button>
                                                                ))}
                                                            </div>
                                                        </>
                                                    )}
                                                </div>

                                                {/* Dropdown Customizado de Ano */}
                                                <div className="relative">
                                                    <button onClick={() => setIsYearSelectOpen(!isYearSelectOpen)} className="flex items-center gap-1 bg-white border border-gray-200 px-2.5 py-1.5 rounded-lg text-xs font-bold text-indigo-950 hover:border-indigo-400 transition-colors shadow-sm min-w-[70px] justify-between">
                                                        {calendarViewDate.getFullYear()}
                                                        <ChevronDownIcon />
                                                    </button>
                                                    {isYearSelectOpen && (
                                                        <>
                                                            <div className="fixed inset-0 z-40" onClick={() => setIsYearSelectOpen(false)} />
                                                            <div className="absolute top-full left-0 mt-1 w-24 bg-white border border-gray-100 rounded-xl shadow-xl z-50 p-1.5 max-h-48 overflow-y-auto custom-scrollbar animate-scale-in origin-top-left flex flex-col gap-0.5">
                                                                {YEARS.map(y => (
                                                                    <button key={y} onClick={() => selectYear(y)} className={`text-left px-3 py-2 rounded-lg text-xs font-semibold transition-colors ${calendarViewDate.getFullYear() === y ? 'bg-indigo-600 text-white' : 'text-indigo-950 hover:bg-indigo-50'}`}>{y}</button>
                                                                ))}
                                                            </div>
                                                        </>
                                                    )}
                                                </div>
                                            </div>

                                            <button onClick={handleNextMonth} className="p-1.5 bg-white border border-gray-200 hover:border-indigo-400 rounded-lg shadow-sm transition-colors text-indigo-950"><ChevronRightIcon /></button>
                                        </div>

                                        <div className="grid grid-cols-7 gap-1 mb-2">
                                            {weekDays.map((d, i) => <div key={`wd-${i}`} className="w-8 text-center font-poppins text-[10px] font-bold text-indigo-950/40">{d}</div>)}
                                        </div>
                                        <div className="grid grid-cols-7 gap-y-2 gap-x-1">
                                            {renderCalendar()}
                                        </div>
                                        <div className="mt-4 pt-4 border-t border-gray-200/50 text-center font-poppins text-[10px] text-indigo-950/70 font-bold uppercase tracking-wider">
                                            {customStartDate ? formatMockDate(customStartDate) : 'Início'} {' - '} {customEndDate ? formatMockDate(customEndDate) : 'Fim'}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="w-px bg-gray-100 flex-shrink-0" />

                            <div className="flex-1 flex flex-col gap-6">
                                <div>
                                    <h4 className="font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-3">Cursos</h4>
                                    <div className="flex flex-col gap-3">
                                        {COURSES.map(course => (
                                            <label key={course} className="flex items-center gap-3 cursor-pointer group">
                                                <input type="checkbox" checked={selectedCourses.includes(course)} onChange={() => toggleCourse(course)} className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer border-gray-300 transition-colors" />
                                                <span className="font-poppins text-sm text-indigo-950 font-medium group-hover:text-indigo-600 transition-colors">{course}</span>
                                            </label>
                                        ))}
                                    </div>
                                </div>

                                <div>
                                    <h4 className="font-poppins text-xs font-bold text-indigo-950/60 uppercase tracking-wider mb-3">Turno Letivo</h4>
                                    <div className="flex flex-col gap-3">
                                        {PERIODS.map(period => (
                                            <label key={period} className="flex items-center gap-3 cursor-pointer group">
                                                <input type="checkbox" checked={selectedPeriods.includes(period)} onChange={() => togglePeriod(period)} className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer border-gray-300 transition-colors" />
                                                <span className="font-poppins text-sm text-indigo-950 font-medium group-hover:text-indigo-600 transition-colors">{period}</span>
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            </div>

                        </div>

                        <div className="flex items-center justify-between p-6 border-t border-gray-100 bg-gray-50 rounded-b-3xl flex-shrink-0">
                            <button
                                onClick={() => { setSelectedCourses([]); setSelectedPeriods([]); setDateFilter('all'); setCustomStartDate(null); setCustomEndDate(null); }}
                                className={`font-poppins text-sm font-semibold transition-colors ${hasActiveFilters ? 'text-red-600 hover:text-red-700' : 'text-gray-400 cursor-not-allowed'}`}
                                disabled={!hasActiveFilters}
                            >
                                Limpar Filtros
                            </button>

                            <button
                                onClick={() => setIsFilterModalOpen(false)}
                                className="bg-indigo-950 text-white px-8 py-3 rounded-xl font-poppins text-sm font-bold hover:bg-indigo-900 transition-all shadow-md hover:shadow-lg"
                            >
                                Aplicar e Fechar
                            </button>
                        </div>

                    </div>
                </div>
            )}

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