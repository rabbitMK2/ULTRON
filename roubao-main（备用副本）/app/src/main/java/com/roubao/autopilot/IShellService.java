package com.roubao.autopilot;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * 手写版 AIDL 生成的接口与 Stub，替代 IShellService.aidl
 */
public interface IShellService extends IInterface {

    void destroy() throws RemoteException;

    String exec(String command) throws RemoteException;

    abstract class Stub extends Binder implements IShellService {
        private static final String DESCRIPTOR = "com.roubao.autopilot.IShellService";

        // 与原 AIDL 保持完全一致的 transaction code
        public static final int TRANSACTION_destroy = 16777114;
        public static final int TRANSACTION_exec = 1;

        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        public static IShellService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IShellService) {
                return (IShellService) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_destroy: {
                    data.enforceInterface(DESCRIPTOR);
                    this.destroy();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_exec: {
                    data.enforceInterface(DESCRIPTOR);
                    String command = data.readString();
                    String result = this.exec(command);
                    reply.writeNoException();
                    reply.writeString(result);
                    return true;
                }
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IShellService {
            private final IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void destroy() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(TRANSACTION_destroy, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public String exec(String command) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                String result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeString(command);
                    mRemote.transact(TRANSACTION_exec, data, reply, 0);
                    reply.readException();
                    result = reply.readString();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }
        }
    }
}

