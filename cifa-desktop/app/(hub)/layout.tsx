// app/(hub)/layout.tsx

import type { Metadata } from "next";
// Caminho relativo exato: sobe de (hub) para app (../), sobe de app para raiz (../), entra em src/components
import Sidebar from "../../src/components/Sidebar";

export const metadata: Metadata = {
    title: "Dashboard - CIFA",
};

export default function HubLayout({
                                      children,
                                  }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        // w-screen e h-screen cravados. overflow-hidden impede qualquer scroll na página inteira.
        <div className="w-screen h-screen overflow-hidden flex bg-[#f8f9fc] font-poppins">

            {/* Barra Lateral Funcional Importada */}
            <Sidebar />

            {/* Área Principal: h-full garante que ela respeite a altura do pai */}
            <main className="flex-1 h-full overflow-hidden flex flex-col relative">
                {children}
            </main>
        </div>
    );
}