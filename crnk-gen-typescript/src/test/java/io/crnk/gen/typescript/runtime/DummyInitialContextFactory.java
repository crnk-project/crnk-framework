package io.crnk.gen.typescript.runtime;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class DummyInitialContextFactory implements InitialContextFactory {

	@Override
	public Context getInitialContext(Hashtable<?, ?> arg0) throws NamingException {
		return new DummyContext();
	}

	class DummyContext implements Context {

		@Override
		public Object addToEnvironment(String propName, Object propVal) throws NamingException {
			return null;
		}

		@Override
		public void bind(Name name, Object obj) throws NamingException {
		}

		@Override
		public void bind(String name, Object obj) throws NamingException {
		}

		@Override
		public void close() throws NamingException {
		}

		@Override
		public Name composeName(Name name, Name prefix) throws NamingException {
			return null;
		}

		@Override
		public String composeName(String name, String prefix) throws NamingException {
			return null;
		}

		@Override
		public Context createSubcontext(Name name) throws NamingException {
			return null;
		}

		@Override
		public Context createSubcontext(String name) throws NamingException {
			return null;
		}

		@Override
		public void destroySubcontext(Name name) throws NamingException {

		}

		@Override
		public void destroySubcontext(String name) throws NamingException {
		}

		@Override
		public Hashtable<?, ?> getEnvironment() throws NamingException {
			return null;
		}

		@Override
		public String getNameInNamespace() throws NamingException {
			return null;
		}

		@Override
		public NameParser getNameParser(Name name) throws NamingException {
			return null;
		}

		@Override
		public NameParser getNameParser(String name) throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
			return null;
		}

		@Override
		public Object lookup(Name name) throws NamingException {
			return null;
		}

		@Override
		public Object lookup(String name) throws NamingException {
			return null;
		}

		@Override
		public Object lookupLink(Name name) throws NamingException {
			return null;
		}

		@Override
		public Object lookupLink(String name) throws NamingException {
			return null;
		}

		@Override
		public void rebind(Name name, Object obj) throws NamingException {
		}

		@Override
		public void rebind(String name, Object obj) throws NamingException {
		}

		@Override
		public Object removeFromEnvironment(String propName) throws NamingException {
			return null;
		}

		@Override
		public void rename(Name oldName, Name newName) throws NamingException {
		}

		@Override
		public void rename(String oldName, String newName) throws NamingException {
		}

		@Override
		public void unbind(Name name) throws NamingException {
		}

		@Override
		public void unbind(String name) throws NamingException {
		}
	}
}
