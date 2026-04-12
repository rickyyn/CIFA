// src/app/layout.tsx

import type { Metadata } from "next";
import { Poppins } from "next/font/google";
import "./globals.css";

// Configuração da fonte do Google com otimização do Next.js
const poppins = Poppins({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
  variable: "--font-poppins", // Esta variável é lida pelo globals.css
});

export const metadata: Metadata = {
  title: "CIFA - Controle Inteligente de Fluxo Acadêmico",
  description: "Sistema automatizado de controle de acesso acadêmico.",
};

export default function RootLayout({
                                     children,
                                   }: Readonly<{
  children: React.ReactNode;
}>) {
  return (
      <html lang="pt-BR">
      {/* A classe antialiased melhora a renderização da fonte no desktop */}
      <body className={`${poppins.variable} font-poppins antialiased`}>
      {children}
      </body>
      </html>
  );
}