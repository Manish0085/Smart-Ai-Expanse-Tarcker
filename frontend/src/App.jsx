// SecureTracker Pro - UI Entry Point
import React, { useState, useEffect, useRef } from 'react';
import {
  Send, Wallet, ArrowUpRight, ArrowDownLeft, PieChart,
  Sparkles, MessageSquare, LayoutDashboard, History,
  ShieldCheck, LogOut, Settings, CreditCard, User as UserIcon,
  PlusCircle, Search, TrendingUp, Bell, Mail, Lock, UserPlus, Table,
  Mic, MicOff, Menu, X
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import axios from 'axios';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  Cell, PieChart as RePie, Pie, Legend, LineChart, Line
} from 'recharts';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';

// --- Sub-Components ---

const DataChart = ({ data, type = 'bar' }) => {
  if (!data || data.length === 0) return <p>No data for chart</p>;

  // Prepare data: Group by category or person
  const chartData = data.map(item => ({
    name: item.note || item.category || item.person || 'Other',
    value: Math.abs(item.amount),
    type: item.person ? 'Udhaar' : 'Expense'
  }));

  return (
    <div style={{ width: '100%', height: 250, marginTop: '15px' }}>
      <ResponsiveContainer width="100%" height="100%">
        {type === 'bar' ? (
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
            <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={10} />
            <YAxis stroke="var(--text-muted)" fontSize={10} />
            <Tooltip
              contentStyle={{ background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '8px' }}
              itemStyle={{ color: 'white' }}
            />
            <Bar dataKey="value" fill="var(--primary)" radius={[4, 4, 0, 0]} />
          </BarChart>
        ) : (
          <RePie>
            <Pie
              data={chartData}
              innerRadius={60}
              outerRadius={80}
              paddingAngle={5}
              dataKey="value"
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={index % 2 === 0 ? 'var(--primary)' : '#818cf8'} />
              ))}
            </Pie>
            <Tooltip />
            <Legend verticalAlign="bottom" height={36} />
          </RePie>
        )}
      </ResponsiveContainer>
    </div>
  );
};

const DataTable = ({ data }) => {
  if (!data || data.length === 0) return <p>No records found</p>;
  return (
    <div className="table-container" style={{ marginTop: '15px', overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.8rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--glass-border)', color: 'var(--text-muted)' }}>
            <th style={{ textAlign: 'left', padding: '8px' }}>Date</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Description</th>
            <th style={{ textAlign: 'right', padding: '8px' }}>Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item, i) => (
            <tr key={i} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
              <td style={{ padding: '8px' }}>{item.date}</td>
              <td style={{ padding: '8px' }}>{item.note || item.category || item.person}</td>
              <td style={{ padding: '8px', textAlign: 'right', fontWeight: 600, color: item.amount < 0 ? '#ef4444' : '#22c55e' }}>
                ${Math.abs(item.amount).toFixed(2)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// --- Main App ---

function App() {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return localStorage.getItem('isAuth') === 'true';
  });
  const [authMode, setAuthMode] = useState('login');
  const [authData, setAuthData] = useState(() => {
    const saved = localStorage.getItem('authData');
    return saved ? JSON.parse(saved) : { email: '', password: '' };
  });
  const [authError, setAuthError] = useState('');
  const [authMessage, setAuthMessage] = useState('');

  const [activeTab, setActiveTab] = useState('dashboard');
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [report, setReport] = useState(null);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchHistory();
      fetchReport();
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isChatOpen) scrollToBottom();
  }, [messages, isChatOpen]);

  // Auth Handlers
  const handleAuth = async (e) => {
    e.preventDefault();
    setAuthError('');
    setAuthMessage('');

    try {
      if (authMode === 'register') {
        const res = await axios.post(`${API_BASE}/auth/register`, authData);
        setAuthMessage(res.data.message);
        setAuthMode('login');
      } else {
        const res = await axios.post(`${API_BASE}/auth/login`, authData);
        localStorage.setItem('isAuth', 'true');
        localStorage.setItem('authData', JSON.stringify(authData));
        setIsAuthenticated(true);
      }
    } catch (err) {
      setAuthError(err.response?.data?.error || 'Authentication failed');
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post(`${API_BASE}/auth/logout`);
    } finally {
      localStorage.removeItem('isAuth');
      localStorage.removeItem('authData');
      setIsAuthenticated(false);
      setMessages([]);
      setReport(null);
    }
  };

  const fetchHistory = async () => {
    try {
      const res = await axios.get(`${API_BASE}/chat/history`);
      const mapped = res.data.map(m => ({
        id: m.id,
        text: m.content,
        sender: m.sender
      }));
      setMessages(mapped.length > 0 ? mapped : [
        { id: 1, text: "Vault secured. How can I assist with your financial tracking today?", sender: 'bot' }
      ]);
    } catch (err) {
      console.error('Failed to fetch history', err);
    }
  };

  const fetchReport = async () => {
    try {
      const response = await axios.post(`${API_BASE}/chat`, { message: "Show report" });
      setReport(response.data.result);
    } catch (e) {
      console.error(e);
    }
  };

  const [isListening, setIsListening] = useState(false);
  const recognitionRef = useRef(null);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      recognitionRef.current = new SpeechRecognition();
      recognitionRef.current.continuous = false;
      recognitionRef.current.interimResults = false;
      // Set lang to hi-IN, but it will pick up English too (Auto-detection is decent)
      recognitionRef.current.lang = 'hi-IN';

      recognitionRef.current.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        setInput(transcript);
        setIsListening(false);
        // Automatically send the message after a short delay to let user see it
        setTimeout(() => {
          handleSend(transcript);
        }, 500);
      };

      recognitionRef.current.onerror = (event) => {
        console.error('Speech recognition error', event.error);
        setIsListening(false);
      };

      recognitionRef.current.onend = () => {
        setIsListening(false);
      };
    }
  }, []);

  const toggleListening = () => {
    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
    } else {
      if (recognitionRef.current) {
        recognitionRef.current.start();
        setIsListening(true);
      } else {
        alert("Voice recognition not supported in this browser.");
      }
    }
  };

  const handleSend = async (manualInput = null) => {
    const textToSend = manualInput || input;
    if (!textToSend.trim() || loading) return;

    const userMsg = { id: Date.now(), text: textToSend, sender: 'user' };
    setMessages(prev => [...prev, userMsg]);
    if (!manualInput) setInput('');
    setLoading(true);

    try {
      const response = await axios.post(`${API_BASE}/chat`, { message: textToSend });
      const result = response.data.result;

      let botResponse = {
        id: Date.now() + 1,
        sender: 'bot',
        text: typeof result === 'string' ? result : (result.message || "Processed."),
        format: result.format || 'TEXT',
        data: [...(result.expenses || []), ...(result.udhaars || [])]
      };

      if (result.intent === 'SHOW_REPORT') {
        setReport(result);
        setActiveTab('dashboard');
      }

      setMessages(prev => [...prev, botResponse]);

      // Refresh dashboard if state changed
      if (result.amount || result.intent === 'DELETE_SUCCESS') {
        fetchReport();
      }

    } catch (error) {
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        text: "Neural link interrupted. Please try again.",
        sender: 'bot'
      }]);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="auth-overlay">
        <div className="auth-mesh" />
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="auth-card">
          <div style={{ textAlign: 'center', marginBottom: '35px' }}>
            <div className="auth-icon-container">
              <ShieldCheck color="white" size={32} strokeWidth={2.5} />
            </div>
            <h2 style={{ margin: '0 0 8px 0', fontSize: '1.8rem', fontWeight: 800 }}>
              SecureTracker <span className="gradient-text">Pro</span>
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', fontWeight: 500 }}>
              {authMode === 'login' ? 'Welcome back to your vault' : 'Start your financial journey'}
            </p>
          </div>

          <form onSubmit={handleAuth}>
            <div className="input-group">
              <div style={{ color: 'var(--primary)', display: 'flex' }}><Mail size={18} /></div>
              <input type="email" required placeholder="name@example.com"
                value={authData.email} onChange={(e) => setAuthData({ ...authData, email: e.target.value })} />
            </div>
            <div className="input-group">
              <div style={{ color: 'var(--primary)', display: 'flex' }}><Lock size={18} /></div>
              <input type="password" required placeholder="Enter secure key"
                value={authData.password} onChange={(e) => setAuthData({ ...authData, password: e.target.value })} />
            </div>

            <AnimatePresence mode="wait">
              {authError && (
                <motion.p initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }} className="error-text">
                  {authError}
                </motion.p>
              )}
              {authMessage && (
                <motion.p initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }} className="success-text">
                  {authMessage}
                </motion.p>
              )}
            </AnimatePresence>

            <button type="submit" className="btn-primary" style={{ width: '100%', marginTop: '10px' }}>
              {authMode === 'login' ? (
                <>Unlock Vault <ArrowUpRight size={18} /></>
              ) : (
                <>Initialize Account <UserPlus size={18} /></>
              )}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: '30px', borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: '20px' }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              {authMode === 'login' ? "Don't have a vault yet?" : "Already a member?"}
              <span onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')} className="auth-link">
                {authMode === 'login' ? 'Create Account' : 'Sign In'}
              </span>
            </p>
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <div className={`sidebar-overlay ${isSidebarOpen ? 'open' : ''}`} onClick={() => setIsSidebarOpen(false)} />
      <aside className={`sidebar ${isSidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <div className="brand">
            <ShieldCheck color="var(--primary)" size={24} />
            <span>Tracker<span className="gradient-text">Pro</span></span>
          </div>
          <button className="mobile-close" onClick={() => setIsSidebarOpen(false)}><X size={20} /></button>
        </div>
        <nav>
          <div className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => { setActiveTab('dashboard'); setIsSidebarOpen(false); }}>
            <LayoutDashboard size={20} /> Dashboard
          </div>
          <div className={`nav-item ${activeTab === 'history' ? 'active' : ''}`} onClick={() => { setActiveTab('history'); setIsSidebarOpen(false); }}>
            <History size={20} /> Vault History
          </div>
          <div className={`nav-item ${activeTab === 'analytics' ? 'active' : ''}`} onClick={() => { setActiveTab('analytics'); setIsSidebarOpen(false); }}>
            <TrendingUp size={20} /> Analytics
          </div>
        </nav>
        <div className="sidebar-footer">
          <div className="nav-item" onClick={handleLogout}><LogOut size={20} /> Logout</div>
        </div>
      </aside>

      <main className="main-content">
        <header>
          <div className="flex-center gap-10">
            <button className="mobile-menu-btn" onClick={() => setIsSidebarOpen(true)}><Menu size={24} /></button>
            <div>
              <h1>Hi, <span className="gradient-text">{authData.email.split('@')[0]}</span></h1>
              <p>Your financial status is healthy.</p>
            </div>
          </div>
          <div className="header-actions">
            <div className="icon-btn"><Bell size={20} /></div>
            <button className="btn-primary" onClick={() => setIsChatOpen(true)}>
              <PlusCircle size={18} /> Add Entry
            </button>
          </div>
        </header>

        {activeTab === 'dashboard' && (
          <div className="dashboard-view">
            <div className="stats-grid">
              <div className="stat-card">
                <span className="stat-label">TOTAL SPENDING</span>
                <h2>${report ? report.totalExpense.toFixed(2) : '0.00'}</h2>
                <span className="stat-delta negative"><ArrowDownLeft size={14} /> Monthly Cap</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">UDHAAR BALANCE</span>
                <h2>${report ? (report.totalUdhaarGiven - report.totalUdhaarTaken).toFixed(2) : '0.00'}</h2>
                <span className="stat-delta positive"><ArrowUpRight size={14} /> Reclaiming</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">ACTIVE LOANS</span>
                <h2>{report ? report.udhaars.filter(u => !u.given).length : 0}</h2>
                <span className="stat-label">Pending attention</span>
              </div>
            </div>

            <div className="content-grid">
              <div className="glass-card main-table">
                <h3>Recent Transactions</h3>
                <DataTable data={report ? [...report.expenses, ...report.udhaars].slice(0, 8) : []} />
              </div>
              <div className="glass-card analytics-mini">
                <h3>Insights</h3>
                <DataChart data={report ? report.expenses : []} type="pie" />
              </div>
            </div>
          </div>
        )}

        {activeTab === 'analytics' && report && (
          <div className="analytics-view">
            <div className="glass-card" style={{ height: '400px' }}>
              <h3>Spending Distribution</h3>
              <DataChart data={report.expenses} type="bar" />
            </div>
          </div>
        )}
      </main>

      <div className={`chat-pane ${isChatOpen ? 'open' : ''}`}>
        <header className="chat-header">
          <div className="flex-center gap-10">
            <Sparkles size={18} color="var(--primary)" />
            <h3>Financial Intelligence</h3>
          </div>
          <button onClick={() => setIsChatOpen(false)} className="close-btn"><LogOut size={18} /></button>
        </header>

        <div className="messages scroll-hide">
          <AnimatePresence>
            {messages.map((msg) => (
              <motion.div key={msg.id} initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
                className={`msg-bubble ${msg.sender}`}>
                <div className="msg-text">{msg.text}</div>
                {msg.format === 'TABLE' && <DataTable data={msg.data} />}
                {msg.format === 'CHART' && <DataChart data={msg.data} type="bar" />}
              </motion.div>
            ))}
          </AnimatePresence>
          <div ref={messagesEndRef} />
        </div>

        <div className="chat-input-area">
          <button
            onClick={toggleListening}
            className={`icon-btn ${isListening ? 'mic-active' : ''}`}
            style={{ marginRight: '10px' }}
          >
            {isListening ? <MicOff size={20} color="#f87171" /> : <Mic size={20} />}
          </button>

          <input type="text" placeholder={isListening ? "Listening..." : "Explain my spending..."} value={input}
            onChange={(e) => setInput(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && handleSend()} />
          <button onClick={handleSend} disabled={loading} className="send-btn">
            <ArrowUpRight size={20} />
          </button>
        </div>
      </div>

      <button className="fab" onClick={() => setIsChatOpen(!isChatOpen)}>
        <MessageSquare size={24} />
      </button>
    </div>
  );
}

export default App;
