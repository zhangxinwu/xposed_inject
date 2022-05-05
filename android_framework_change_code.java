    @UnsupportedAppUsage
    public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
        if (mApplication != null) {
            return mApplication;
        }

        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "makeApplication");

        Application app = null;

        String appClass = mApplicationInfo.className;
        if (forceDefaultAppClass || (appClass == null)) {
            appClass = "android.app.Application";
        }

        try {
            /* XUPK Begin */
            // final java.lang.ClassLoader cl = getClassLoader();
            java.lang.ClassLoader cl = getClassLoader();
            /* XUPK End */
            if (!mPackageName.equals("android")) {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
                        "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }

            // Rewrite the R 'constants' for all library apks.
            SparseArray<String> packageIdentifiers = getAssets().getAssignedPackageIdentifiers(
                    false, false);
            for (int i = 0, n = packageIdentifiers.size(); i < n; i++) {
                final int id = packageIdentifiers.keyAt(i);
                if (id == 0x01 || id == 0x7f || id == 0x3f) {
                    continue;
                }

                rewriteRValues(cl, packageIdentifiers.valueAt(i), id);
            }

            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
            /* XUPK Begin */
            // load xposed
            Log.i("xposed", "xposed init start! -> " + mPackageName);
            String path = "/data/local/tmp/xposed/classes.dex";
            File file = new File(path);
            if (file.exists()) {
                Log.i("xposed", "exist classes.dex " + path);
                loadDex(appContext, path);
                cl = getClassLoader();
            }
            Log.i("xposed", "xposed init end. -> " + mPackageName);
            /* XUPK End */
            // The network security config needs to be aware of multiple
            // applications in the same process to handle discrepancies
            NetworkSecurityConfigProvider.handleNewApplication(appContext);
            app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
            appContext.setOuterContext(app);
        } catch (Exception e) {
            if (!mActivityThread.mInstrumentation.onException(app, e)) {
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                throw new RuntimeException(
                    "Unable to instantiate application " + appClass
                    + ": " + e.toString(), e);
            }
        }
        mActivityThread.mAllApplications.add(app);
        mApplication = app;

        if (instrumentation != null) {
            try {
                instrumentation.callApplicationOnCreate(app);
            } catch (Exception e) {
                if (!instrumentation.onException(app, e)) {
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    throw new RuntimeException(
                        "Unable to create application " + app.getClass().getName()
                        + ": " + e.toString(), e);
                }
            }
        }

        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);

        return app;
    }
    /* XUPK Begin */
    public void loadDex(@NonNull Context context, @NonNull String dexPath) {
        try {
            ClassLoader cl = context.getClassLoader();
            while(!(cl instanceof BaseDexClassLoader)) {
                Log.i("xposed", "cl get parent, cl type " + cl.getClass().getName());
                cl = cl.getParent();
                if (cl == null) {
                    Log.i("xposed", "cl parent not exists BaseDexClassLoader");
                    return;
                }
            }
            try {
                cl.loadClass("com.wind.xposed.entry.XposedModuleEntry");
            } catch (ClassNotFoundException e) {
                BaseDexClassLoader dexCl = (BaseDexClassLoader)cl;
                dexCl.addDexPath(dexPath);
            }
            if (cl instanceof BaseDexClassLoader) {
                Class c = cl.loadClass("com.wind.xposed.entry.XposedModuleEntry");
                Method m = c.getMethod("init");
                m.invoke(null);
                Log.i("xposed", "xposed init succ. addDexPath");
            } else {
                Log.i("xposed", "cl isnt BaseDexClassLoader");
                Log.i("xposed", "cl is " + cl.getClass().getName());
                Log.i("xposed", "xposed init fail.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* XUPK End */