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
	public Context getInitialContext(Hashtable<?, ?> arg0) {
		return new DummyContext();
	}

	class DummyContext implements Context {

		@Override
		public Object addToEnvironment(String propName, Object propVal) {
			return null;
		}

		@Override
		public void bind(Name name, Object obj) {
		}

		@Override
		public void bind(String name, Object obj) {
		}

		@Override
		public void close() {
		}

		@Override
		public Name composeName(Name name, Name prefix) {
			return null;
		}

		@Override
		public String composeName(String name, String prefix) {
			return null;
		}

		@Override
		public Context createSubcontext(Name name) {
			return null;
		}

		@Override
		public Context createSubcontext(String name) {
			return null;
		}

		@Override
		public void destroySubcontext(Name name) {

		}

		@Override
		public void destroySubcontext(String name) {
		}

		@Override
		public Hashtable<?, ?> getEnvironment() {
			return null;
		}

		@Override
		public String getNameInNamespace() {
			return null;
		}

		@Override
		public NameParser getNameParser(Name name) {
			return null;
		}

		@Override
		public NameParser getNameParser(String name) {
			return null;
		}

		@Override
		public NamingEnumeration<NameClassPair> list(Name name) {
			return null;
		}

		@Override
		public NamingEnumeration<NameClassPair> list(String name) {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(Name name) {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(String name) {
			return null;
		}

		@Override
		public Object lookup(Name name) {
			return null;
		}

		@Override
		public Object lookup(String name) {
			return null;
		}

		@Override
		public Object lookupLink(Name name) {
			return null;
		}

		@Override
		public Object lookupLink(String name) {
			return null;
		}

		@Override
		public void rebind(Name name, Object obj) {
		}

		@Override
		public void rebind(String name, Object obj) {
		}

		@Override
		public Object removeFromEnvironment(String propName) {
			return null;
		}

		@Override
		public void rename(Name oldName, Name newName) {
		}

		@Override
		public void rename(String oldName, String newName) {
		}

		@Override
		public void unbind(Name name) {
		}

		@Override
		public void unbind(String name) {
		}
	}
}
