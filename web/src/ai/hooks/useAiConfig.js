import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { aiAPI } from '../api/aiService';

let cachedUserId = null;
let cachedConfig = undefined;
let cachedConfigs = [];
let cachedSelectedId = null;

const listeners = new Set();

const notify = () => listeners.forEach((fn) => fn());

const applyListPayload = (data) => {
  const list = data?.configs ?? [];
  const sid = data?.selectedConfigId ?? null;
  cachedConfigs = list;
  cachedSelectedId = sid;
  const active = (
    list.find((c) => c.id === sid)
    || list.find((c) => c.hasApiKey)
    || list[0]
    || null
  );
  cachedConfig = active ?? null;
};

export const useAiConfig = () => {
  const { user, isAuthReady } = useAuth();
  const userId = user?.id ?? null;

  const [config, setConfig] = useState(() => (
    cachedUserId === userId ? cachedConfig : undefined
  ));
  const [configs, setConfigs] = useState(() => (
    cachedUserId === userId ? cachedConfigs : []
  ));
  const [selectedConfigId, setSelectedConfigId] = useState(() => (
    cachedUserId === userId ? cachedSelectedId : null
  ));
  const [loading, setLoading] = useState(() => {
    if (!isAuthReady) return true;
    if (!userId) return false;
    return cachedUserId !== userId || cachedConfig === undefined;
  });

  useEffect(() => {
    const handler = () => {
      setConfig(cachedConfig);
      setConfigs(cachedConfigs);
      setSelectedConfigId(cachedSelectedId);
    };
    listeners.add(handler);
    return () => listeners.delete(handler);
  }, []);

  useEffect(() => {
    if (!isAuthReady) {
      return;
    }

    if (!userId) {
      cachedUserId = null;
      cachedConfig = undefined;
      cachedConfigs = [];
      cachedSelectedId = null;
      setConfig(null);
      setConfigs([]);
      setSelectedConfigId(null);
      setLoading(false);
      return;
    }

    if (cachedUserId !== userId) {
      cachedUserId = userId;
      cachedConfig = undefined;
      cachedConfigs = [];
      cachedSelectedId = null;
    }

    let cancelled = false;
    setLoading(true);

    (async () => {
      try {
        const res = await aiAPI.listAiConfigs();
        if (cancelled) return;

        if (res.success && res.data) {
          applyListPayload(res.data);
        } else {
          cachedConfigs = [];
          cachedSelectedId = null;
          cachedConfig = null;
        }
      } catch {
        if (!cancelled) {
          cachedConfigs = [];
          cachedSelectedId = null;
          cachedConfig = null;
        }
      } finally {
        if (!cancelled) {
          setConfig(cachedConfig);
          setConfigs([...cachedConfigs]);
          setSelectedConfigId(cachedSelectedId);
          setLoading(false);
          notify();
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isAuthReady, userId]);

  const saveConfig = useCallback(async ({ id, name, model, proxyUrl, apiKey }) => {
    const res = await aiAPI.saveAiConfig({ id, name, model, proxyUrl, apiKey });
    if (res.success) {
      const savedConfigId = res.data?.id ?? id ?? null;
      // Only auto-select on create (no id was provided), not on edits.
      if (id == null && savedConfigId != null) {
        try {
          await aiAPI.selectAiConfig(savedConfigId);
        } catch {
          // Continue and refresh from list endpoint below.
        }
      }
      const listRes = await aiAPI.listAiConfigs();
      if (listRes.success && listRes.data) {
        applyListPayload(listRes.data);
      }
      notify();
    }
    return res;
  }, []);

  const deleteConfig = useCallback(async (configId) => {
    const res = await aiAPI.deleteAiConfig(configId);
    if (res.success) {
      const listRes = await aiAPI.listAiConfigs();
      if (listRes.success && listRes.data) {
        applyListPayload(listRes.data);
      } else {
        cachedConfigs = cachedConfigs.filter((c) => c.id !== configId);
        if (cachedSelectedId === configId) {
          cachedSelectedId = cachedConfigs[0]?.id ?? null;
        }
        cachedConfig = cachedConfigs.find((c) => c.id === cachedSelectedId) || cachedConfigs[0] || null;
      }
      notify();
    }
    return res;
  }, []);

  const selectConfig = useCallback(async (configId) => {
    const res = await aiAPI.selectAiConfig(configId);
    if (res.success) {
      const listRes = await aiAPI.listAiConfigs();
      if (listRes.success && listRes.data) {
        applyListPayload(listRes.data);
      }
      notify();
    }
    return res;
  }, []);

  const refetch = useCallback(async () => {
    try {
      const res = await aiAPI.listAiConfigs();
      if (res.success && res.data) {
        applyListPayload(res.data);
      } else {
        cachedConfigs = [];
        cachedSelectedId = null;
        cachedConfig = null;
      }
    } catch {
      cachedConfigs = [];
      cachedSelectedId = null;
      cachedConfig = null;
    }
    notify();
  }, []);

  return {
    config,
    configs,
    selectedConfigId,
    loading,
    isConfigured: config != null && config.hasApiKey,
    saveConfig,
    deleteConfig,
    selectConfig,
    refetch,
  };
};
