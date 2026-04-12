// src/components/Sidebar.tsx

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

// Componente para os ícones
const Icon = ({ path }: { path: string }) => (
    <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d={path} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
);

// Mapeamento das rotas reais da aplicação
const navItems = [
    { name: 'Dashboard', href: '/dashboard', iconPath: 'M3 3h7v7H3zm11 0h7v7h-7zm0 11h7v7h-7zm-11 0h7v7H3z' },
    { name: 'Estudantes', href: '/estudantes', iconPath: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M12 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75' },
    { name: 'Cadastro', href: '/cadastro', iconPath: 'M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M12 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0z M20 8v6 M23 11h-6' },
    { name: 'Relatórios', href: '/logs', iconPath: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6' },
];

export default function Sidebar() {
    const pathname = usePathname(); // Captura a rota atual para o estado "ativo"

    return (
        <aside className="w-[280px] bg-indigo-950 flex flex-col justify-between p-8 flex-shrink-0 h-full">
            <div>
                {/* Identidade Visual */}
                <div className="flex items-center gap-3 mb-10">
                    <div className="text-white font-bold text-2xl tracking-wide">CIFA</div>
                    <p className="text-indigo-200 text-[10px] leading-tight">
                        Controle Inteligente <br /> de Fluxo Acadêmico
                    </p>
                </div>

                {/* Navegação Principal */}
                <nav className="flex flex-col gap-2">
                    {navItems.map((item) => {
                        // Verifica se a rota atual começa com o href do item para mantê-lo ativo
                        const isActive = pathname.startsWith(item.href);

                        return (
                            <Link
                                key={item.name}
                                href={item.href}
                                className={`flex items-center gap-4 px-5 py-3.5 rounded-xl text-base font-medium transition-colors ${
                                    isActive
                                        ? 'bg-white text-indigo-950 shadow-md'
                                        : 'text-white hover:bg-indigo-900/50'
                                }`}
                            >
                                <Icon path={item.iconPath} />
                                {item.name}
                            </Link>
                        );
                    })}

                    <div className="border-t border-indigo-800/50 my-2" />

                    <Link
                        href="/administracao"
                        className="flex items-center gap-4 px-5 py-3.5 rounded-xl text-white text-base font-medium hover:bg-indigo-900/50 transition-colors"
                    >
                        <Icon path="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 0 0-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 0 0-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 0 0-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 0 0-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 0 0 1.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z" />
                        Administração
                    </Link>
                </nav>
            </div>

            {/* Rodapé do Sidebar */}
            <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full border border-indigo-700/50 flex items-center justify-center text-white/50 font-bold text-lg">
                    N
                </div>
                <Link
                    href="/"
                    className="flex-1 flex items-center gap-3 px-4 py-3 rounded-xl text-white text-base font-medium hover:bg-indigo-900/50 transition-colors"
                >
                    <Icon path="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9" />
                    Sair
                </Link>
            </div>
        </aside>
    );
}