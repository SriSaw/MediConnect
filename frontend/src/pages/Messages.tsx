import React, { useState, useEffect } from 'react';
import { messageService } from '../services/api';
import { Message, ConversationSummary } from '../types';
import { useAuth } from '../context/AuthContext';
import { useSearchParams } from 'react-router-dom';
import { Send, User as UserIcon, MessageSquare } from 'lucide-react';

export const Messages: React.FC = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const targetUserIdParam = searchParams.get('userId');

  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [activeUserId, setActiveUserId] = useState<number | null>(
    targetUserIdParam ? Number(targetUserIdParam) : null
  );
  const [messages, setMessages] = useState<Message[]>([]);
  const [content, setContent] = useState('');
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchConversations = async () => {
    try {
      const convs = await messageService.getConversations();
      setConversations(convs);
      if (!activeUserId && convs.length > 0) {
        setActiveUserId(convs[0].otherUserId);
      }
    } catch {
      setConversations([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchThread = async (otherId: number) => {
    try {
      const res = await messageService.getConversationWithUser(otherId, 0, 100);
      setMessages(res.content);
    } catch {
      setMessages([]);
    }
  };

  useEffect(() => {
    fetchConversations();
  }, []);

  useEffect(() => {
    if (activeUserId) {
      fetchThread(activeUserId);
    }
  }, [activeUserId]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUserId || !content.trim()) return;
    setSending(true);
    try {
      await messageService.send({
        receiverId: activeUserId,
        content: content.trim(),
      });
      setContent('');
      await fetchThread(activeUserId);
      await fetchConversations();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to send message');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden flex flex-col md:flex-row h-[700px]">
      {/* Sidebar: conversation list */}
      <div className="w-full md:w-80 border-r border-slate-200 flex flex-col">
        <div className="p-4 border-b border-slate-200">
          <h2 className="font-bold text-slate-800 text-base flex items-center gap-2">
            <MessageSquare className="w-5 h-5 text-teal-600" /> Conversations
          </h2>
        </div>

        <div className="flex-1 overflow-y-auto divide-y divide-slate-100">
          {conversations.length === 0 ? (
            <div className="p-6 text-center text-xs text-slate-400">
              No active conversations. Start chatting from doctor or patient profiles!
            </div>
          ) : (
            conversations.map((c) => (
              <button
                key={c.otherUserId}
                onClick={() => setActiveUserId(c.otherUserId)}
                className={`w-full text-left p-4 hover:bg-slate-50 transition-colors flex items-start gap-3 ${
                  activeUserId === c.otherUserId ? 'bg-teal-50/60 border-l-4 border-teal-600' : ''
                }`}
              >
                <div className="w-9 h-9 rounded-full bg-slate-200 text-slate-700 flex items-center justify-center font-bold text-sm flex-shrink-0">
                  {c.otherUserName.charAt(0)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-baseline">
                    <p className="font-semibold text-xs text-slate-900 truncate">{c.otherUserName}</p>
                    {c.unreadCount > 0 && (
                      <span className="w-4 h-4 rounded-full bg-teal-600 text-white text-[10px] flex items-center justify-center font-bold">
                        {c.unreadCount}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 truncate mt-0.5">{c.lastMessage}</p>
                </div>
              </button>
            ))
          )}
        </div>
      </div>

      {/* Main chat window */}
      <div className="flex-1 flex flex-col">
        {activeUserId ? (
          <>
            <div className="p-4 border-b border-slate-200 flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-teal-600 text-white flex items-center justify-center font-bold text-xs">
                #
              </div>
              <h3 className="font-bold text-sm text-slate-800">
                {conversations.find((c) => c.otherUserId === activeUserId)?.otherUserName ||
                  `User #${activeUserId}`}
              </h3>
            </div>

            <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-slate-50/50">
              {messages.length === 0 ? (
                <div className="text-center py-12 text-xs text-slate-400">
                  No messages yet. Send a direct message to begin.
                </div>
              ) : (
                messages.map((m) => {
                  const isMe = m.senderId === user?.id;
                  return (
                    <div
                      key={m.id}
                      className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}
                    >
                      <div
                        className={`max-w-md px-4 py-2.5 rounded-2xl text-xs ${
                          isMe
                            ? 'bg-teal-600 text-white rounded-br-none'
                            : 'bg-white text-slate-800 border border-slate-200 rounded-bl-none shadow-sm'
                        }`}
                      >
                        <p>{m.content}</p>
                      </div>
                      <span className="text-[10px] text-slate-400 mt-1 px-1">
                        {m.createdAt.substring(11, 16)}
                      </span>
                    </div>
                  );
                })
              )}
            </div>

            <form onSubmit={handleSend} className="p-4 border-t border-slate-200 bg-white flex gap-2">
              <input
                type="text"
                required
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Type your secure message..."
                className="flex-1 px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
              <button
                type="submit"
                disabled={sending}
                className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium flex items-center gap-1.5 shadow-sm disabled:opacity-50"
              >
                <Send className="w-4 h-4" />
                <span>Send</span>
              </button>
            </form>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-slate-400 text-sm">
            Select a conversation to start chatting.
          </div>
        )}
      </div>
    </div>
  );
};
