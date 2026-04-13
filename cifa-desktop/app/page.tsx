// src/app/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(false);

    // Simulação de delay de rede para dar realismo à apresentação
    setTimeout(() => {
      if (email === 'admin' && password === '1234') {
        // Salva a sessão no navegador
        localStorage.setItem('cifa_auth', 'true');
        localStorage.setItem('user_name', 'Vinicius');
        router.push('/dashboard');
      } else {
        setError(true);
        setLoading(false);
      }
    }, 800);
  };

  return (
      <main className="min-h-screen grid grid-cols-2 font-poppins overflow-hidden">
        {/* Coluna Esquerda: Identidade */}
        <div className="bg-indigo-950 p-24 flex flex-col justify-between relative overflow-hidden">
          {/* Fundo decorativo simulando o asset que você inserirá */}
          <div className="absolute inset-0 bg-gradient-to-br from-indigo-600/20 to-transparent pointer-events-none" />

          <div className="flex items-center gap-3 relative z-10">
            <div className="text-white font-bold text-3xl">CIFA</div>
            <p className="text-indigo-200 text-sm leading-tight">
              Controle Inteligente <br /> de Fluxo Acadêmico
            </p>
          </div>

          <div className="flex-grow flex flex-col justify-center gap-6 relative z-10">
            <h1 className="text-white text-7xl font-bold leading-tight">
              Bem vindo <br />
              de volta! <span className="text-6xl">👋</span>
            </h1>
            <p className="text-indigo-100 text-lg max-w-lg leading-relaxed">
              Simplifique processos, organize entradas e saídas e tenha o controle da Fatec na palma da mão.
            </p>
          </div>

          <div className="text-indigo-400 text-sm relative z-10">CIFA © 2026</div>
        </div>

        {/* Coluna Direita: Formulário */}
        <div className="bg-white p-24 flex flex-col items-center justify-center">
          <form onSubmit={handleLogin} className="w-full max-w-sm flex flex-col gap-6">
            <div className="flex flex-col gap-1">
              <h2 className="text-indigo-950 text-xl font-bold">Entrar no CIFA</h2>
              <p className="text-indigo-950/40 text-sm font-medium">Use suas credenciais de administrador</p>
            </div>

            <div className="flex flex-col gap-4">
              <div className="relative group">
                <input
                    type="text"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Usuário (admin)"
                    className={`w-full px-6 py-4 rounded-xl border ${error ? 'border-red-500 bg-red-50' : 'border-indigo-100 bg-indigo-50/50'} text-indigo-950 placeholder:text-indigo-300 outline-none transition-all focus:ring-2 focus:ring-indigo-600/20`}
                    required
                />
              </div>

              <div className="relative group">
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Senha (1234)"
                    className={`w-full px-6 py-4 rounded-xl border ${error ? 'border-red-500 bg-red-50' : 'border-indigo-100 bg-indigo-50/50'} text-indigo-950 placeholder:text-indigo-300 outline-none transition-all focus:ring-2 focus:ring-indigo-600/20`}
                    required
                />
              </div>
            </div>

            {error && (
                <p className="text-red-600 text-xs font-bold animate-shake bg-red-100 p-3 rounded-lg">
                  Usuário ou senha incorretos. Tente novamente.
                </p>
            )}

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" className="w-4 h-4 rounded text-indigo-600 border-gray-300 focus:ring-indigo-500" />
                <span className="text-indigo-950/60 text-sm font-medium">Lembrar de mim</span>
              </label>
              <a href="#" className="text-indigo-600 text-sm font-bold hover:text-indigo-800 transition-colors">
                Esqueceu a senha?
              </a>
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full bg-indigo-950 text-white py-4 px-6 rounded-2xl font-bold text-base hover:bg-indigo-900 transition-all shadow-lg active:scale-[0.98] disabled:opacity-50 disabled:cursor-wait"
            >
              {loading ? 'Validando...' : 'Entrar'}
            </button>
          </form>
        </div>

        <style jsx global>{`
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-4px); }
          75% { transform: translateX(4px); }
        }
        .animate-shake { animation: shake 0.2s ease-in-out 0s 2; }
      `}</style>
      </main>
  );
}