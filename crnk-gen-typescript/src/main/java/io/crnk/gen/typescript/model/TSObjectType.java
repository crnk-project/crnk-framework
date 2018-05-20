package io.crnk.gen.typescript.model;

import java.util.*;

public abstract class TSObjectType extends TSTypeBase implements TSExportedElement {

	private List<TSMember> declaredMembers = new ArrayList<>();

	private List<TSInterfaceType> implementedInterfaces = new ArrayList<>();

	private boolean exported;

	private TSIndexSignatureType indexSignature;

	public List<TSMember> getDeclaredMembers() {
		return declaredMembers;
	}

	public void addDeclaredMember(TSMember member) {
		declaredMembers.add(member);
		member.setParent(this);
	}

	public TSMember getDeclaredMember(String name) {
		for (TSMember member : declaredMembers) {
			if (name.equals(member.getName())) {
				return member;
			}
		}
		return null;
	}

	public List<TSMember> getMembers() {
		List<TSMember> members = new ArrayList<>();

		Map<String, TSMember> memberMap = new HashMap<>();

		for (TSMember member : declaredMembers) {
			members.add(member);
			memberMap.put(member.getName(), member);
		}

		for (TSInterfaceType implementedInterface : implementedInterfaces) {
			List<TSMember> implementedMembers = implementedInterface.getMembers();
			for (TSMember implementedMember : implementedMembers) {
				if (memberMap.containsKey(implementedMember.getName())) {
					continue;
				}
				members.add(implementedMember);
				memberMap.put(implementedMember.getName(), implementedMember);
			}
		}

		return members;
	}

	public void addImplementedInterface(TSInterfaceType type) {
		if (!implementedInterfaces.contains(type)) {
			implementedInterfaces.add(type);
		}
	}

	public List<TSInterfaceType> getImplementedInterfaces() {
		return Collections.unmodifiableList(implementedInterfaces);
	}

	public void setImplementedInterfaces(List<TSInterfaceType> implementedInterfaces) {
		this.implementedInterfaces = implementedInterfaces;
	}

	@Override
	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	public TSIndexSignatureType getIndexSignature() {
		return indexSignature;
	}

	public void setIndexSignature(TSIndexSignatureType indexSignature) {
		this.indexSignature = indexSignature;
	}

	public List<TSField> getFields() {
		List<TSField> fields = new ArrayList<>();
		for (TSMember member : getMembers()) {
			if (member instanceof TSField) {
				fields.add((TSField) member);
			}
		}
		return fields;
	}


	public boolean implementsInterface(TSInterfaceType interfaceType) {
		if (this.equals(interfaceType)) {
			return true;
		}

		for (TSInterfaceType implementedInterface : implementedInterfaces) {
			if (implementedInterface.implementsInterface(interfaceType)) {
				return true;
			}
		}
		return false;
	}

}
