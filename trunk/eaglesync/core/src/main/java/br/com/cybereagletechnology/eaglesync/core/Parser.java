package br.com.cybereagletechnology.eaglesync.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import br.com.cybereagletechnology.eaglesync.exception.CollectionTypeNotSupportedException;
import br.com.cybereagletechnology.eaglesync.exception.ConstructionException;
import br.com.cybereagletechnology.eaglesync.exception.FieldNotFoundException;
import br.com.cybereagletechnology.eaglesync.exception.IdNotFoundException;
import br.com.cybereagletechnology.eaglesync.exception.IdTypeNotSupportedException;
import br.com.cybereagletechnology.eaglesync.exception.ImplementationCollectionException;
import br.com.cybereagletechnology.eaglesync.exception.InvalidDateException;
import br.com.cybereagletechnology.eaglesync.exception.InvalidTypeException;
import br.com.cybereagletechnology.eaglesync.exception.InvalidXmlException;
import br.com.cybereagletechnology.eaglesync.exception.NotYetSupportedException;
import br.com.cybereagletechnology.eaglesync.exception.NullIdException;
import br.com.cybereagletechnology.eaglesync.exception.ObjectClassNotFoundException;
import br.com.cybereagletechnology.eaglesync.exception.XmlAnnotationNotFoundException;
import br.com.cybereagletechnology.eaglesync.metadata.ServerId;
import br.com.cybereagletechnology.eaglesync.metadata.ParseType;
import br.com.cybereagletechnology.eaglesync.metadata.Transient;
import br.com.cybereagletechnology.eaglesync.metadata.XML;
import br.com.cybereagletechnology.eaglesync.util.ReflectionUtils;

public class Parser {

	/**
	 * Método usado para se serializar um objeto e obter sua representação XML. 
	 * Com esse método, o XML é formatado de forma legível.
	 * @param source
	 *            Objeto a ser serializado
	 * @return XML formatado
	 * 
	 * @author Fernando Camargo
	 * 
	 */
	public static String parsePrettyXML(Object source) {
		Document document = parseDocument(source);
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		return out.outputString(document);
	}
	
	/**
	 * Método usado para se serializar um objeto e obter sua representação XML. 
	 * Com esse método, o XML é formatado de forma compacta.
	 * @param source
	 *            Objeto a ser serializado
	 * @return XML formatado
	 */
	public static String parseXML(Object source) {
		Document document = parseDocument(source);
		XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
		return out.outputString(document);
	}

	/**
	 * Método que retorna um {@link org.jdom2.Document} representando o objeto
	 * serializado. Esse {@link org.jdom2.Document} possui uma tag <b>beans</b>
	 * como raiz com duas tags filhas: <b>object</b> (objeto sendo serializado)
	 * e <b>referencedObjects</b> (coleção de objetos referenciados direta ou
	 * indiretamente pelo objeto sendo serializado). Dessa forma, é garantido
	 * que um objeto referenciado pelo objeto sendo serializado não será
	 * serializado mais de uma vez.
	 * 
	 * @param source
	 *            Objeto a ser serializado.
	 * @return Representação XML do objeto serializado.
	 * @throws Exception
	 */
	private static Document parseDocument(Object source) {
		Element rootElement = new Element(XMLConstants.ROOT);
		Document document = new Document(rootElement);
		Element referencedObjectsElement = new Element(
				XMLConstants.REFERENCED_OBJECTS);
		Set<ObjectIdentifier> referencedObjectsSet = new HashSet<ObjectIdentifier>();
		Element objectElement = parseElement(source, referencedObjectsElement,
				referencedObjectsSet);
		rootElement.addContent(objectElement);
		rootElement.addContent(referencedObjectsElement);
		document.setDocType(new DocType("XML-Object"));
		return document;
	}

	/**
	 * Método responsável por criar um {@link org.jdom2.Element} que represente
	 * um objeto e adicionar seus objetos referenciados no
	 * referencedObjectsElement.
	 * 
	 * @param source
	 *            Objeto a ser serializado
	 * @param referencedObjectsSet
	 * @param referencedObjectsElement
	 * @return Element do objeto serializado
	 */
	private static Element parseElement(Object source,
			Element referencedObjectsElement,
			Set<ObjectIdentifier> referencedObjectsSet) {
		Class<?> sourceClass = source.getClass();

		XML xmlClassAnnotation = sourceClass.getAnnotation(XML.class);
		if (xmlClassAnnotation == null) {
			throw new XmlAnnotationNotFoundException(
					"XML Annotation not found at: " + sourceClass.getName());
		}
		ParseType defaultParseType = xmlClassAnnotation.parseType();

		List<Field> instanceVariables = ReflectionUtils
				.getInstanceVariables(sourceClass);

		String id = getObjectId(sourceClass, instanceVariables, source);

		Element objElement = new Element(XMLConstants.OBJECT);
		objElement.setAttribute(XMLConstants.CLASS, sourceClass.getName());
		if (id != null) {
			objElement.setAttribute(XMLConstants.ID, id);
		}

		removeTransientFields(instanceVariables);

		for (Field field : instanceVariables) {
			Element fieldElement = new Element(XMLConstants.FIELD);
			fieldElement.setAttribute(XMLConstants.NAME, field.getName());

			Object fieldValue = extractFieldValue(sourceClass, source, field);
			Class<?> fieldType = field.getType();
			Type fieldGenericType = field.getGenericType();
			if (isCommonType(fieldType)) {
				fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
						XMLConstants.VALUE_FIELD_TYPE);
				setElementText(fieldElement, fieldValue);
			} else {
				XML xmlFieldAnnotation = field.getAnnotation(XML.class);
				// Pega o ParseType do campo. Caso não esteja definido, pega o
				// definido na classe.
				ParseType parseType = xmlFieldAnnotation != null ? xmlFieldAnnotation
						.parseType() : defaultParseType;
				if (isCollectionType(fieldValue)) {
					// Devemos descobrir o tipo da Collection
					Class<?> collectionType = getCollectionType(fieldGenericType);
					Collection<?> collectionValue = (Collection<?>) fieldValue;
					if (isCommonType(collectionType)) {
						fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
								XMLConstants.VALUE_COLLECTION_FIELD_TYPE);
						if (collectionValue != null) {
							for (Object value : collectionValue) {
								addValueElement(fieldElement, value);
							}
						} else {
							setElementText(fieldElement, null);
						}
					} else {
						switch (parseType) {
						case COMPLETE:
							fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
									XMLConstants.OBJECT_COLLECTION_FIELD_TYPE);
							if (collectionValue != null) {
								for (Object value : collectionValue) {
									Element referenceElement = getReference(
											referencedObjectsElement,
											referencedObjectsSet,
											collectionType, value);
									fieldElement.addContent(referenceElement);
								}
							} else {
								setElementText(fieldElement, null);
							}
							break;
						case INCOMPLETE:
							fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
									XMLConstants.ID_COLLECTION_FIELD_TYPE);
							if (collectionValue != null) {
								for (Object value : collectionValue) {
									addIdElement(fieldElement, collectionType,
											value);
								}
							} else {
								setElementText(fieldElement, null);
							}
							break;
						}
					}
				} else if (isArrayType(fieldType)) {
					Class<?> componentType = fieldType.getComponentType();
					if (isArrayType(componentType)) {
						throw new NotYetSupportedException(
								"n dimensions array type");
					}
					if (isCommonType(componentType)) {
						fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
								XMLConstants.VALUE_ARRAY_FIELD_TYPE);
						if (fieldValue != null) {
							int arrayLength = Array.getLength(fieldValue);
							for (int i = 0; i < arrayLength; i++) {
								addValueElement(fieldElement,
										Array.get(fieldValue, i));
							}
						} else {
							setElementText(fieldElement, null);
						}
					} else {
						switch (parseType) {
						case COMPLETE:
							fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
									XMLConstants.OBJECT_ARRAY_FIELD_TYPE);
							if (fieldValue != null) {
								int arrayLength = Array.getLength(fieldValue);
								for (int i = 0; i < arrayLength; i++) {
									Element referenceElement = getReference(
											referencedObjectsElement,
											referencedObjectsSet,
											componentType,
											Array.get(fieldValue, i));
									fieldElement.addContent(referenceElement);
								}
							} else {
								setElementText(fieldElement, null);
							}
							break;
						case INCOMPLETE:
							fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
									XMLConstants.ID_ARRAY_FIELD_TYPE);
							if (fieldValue != null) {
								int arrayLength = Array.getLength(fieldValue);
								for (int i = 0; i < arrayLength; i++) {
									addIdElement(fieldElement, componentType,
											Array.get(fieldValue, i));
								}
							} else {
								setElementText(fieldElement, null);
							}
							break;
						}
					}
				} else {

					switch (parseType) {
					case COMPLETE:
						fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
								XMLConstants.OBJECT_FIELD_TYPE);
						if (fieldValue == null) {
							setElementText(fieldElement, null);
							break;
						}

						Element referenceElement = getReference(
								referencedObjectsElement, referencedObjectsSet,
								fieldType, fieldValue);
						fieldElement.addContent(referenceElement);
						break;
					case INCOMPLETE:
						fieldElement.setAttribute(XMLConstants.FIELD_TYPE,
								XMLConstants.ID_FIELD_TYPE);
						if (fieldValue == null) {
							setElementText(fieldElement, null);
							break;
						}
						Object fieldId = getObjectId(
								fieldType,
								ReflectionUtils.getInstanceVariables(fieldType),
								fieldValue);
						if (isCommonType(fieldId)) {
							fieldElement.setText(fieldId.toString());
						} else if (fieldId == null) {
							throw new NullIdException(fieldType.getName());
						} else {
							throw new IdTypeNotSupportedException(
									fieldType.getName());
						}
						break;
					}

				}
			}

			objElement.addContent(fieldElement);
		}

		return objElement;
	}

	/**
	 * Método que faz validações no XML e retorna um Objeto desserializado.
	 * 
	 * @param xml
	 *            XML gerado no momento da serialização
	 * @return Objeto desserializado
	 */
	public static Object parseObject(String xml) {
		SAXBuilder saxBuilder = new SAXBuilder();
		try {
			Reader in = new StringReader(xml);
			Document document = saxBuilder.build(in);
			Element rootElement = document.getRootElement();
			if (!rootElement.getName().equals(XMLConstants.ROOT)) {
				throw new InvalidXmlException("Invalid root element");
			}
			Element objectElement = rootElement.getChild(XMLConstants.OBJECT);
			if (objectElement == null) {
				throw new InvalidXmlException("Object element not found");
			}
			Element referencedObjectsElement = rootElement
					.getChild(XMLConstants.REFERENCED_OBJECTS);
			if (referencedObjectsElement == null) {
				throw new InvalidXmlException(
						"Referenced objects element not found");
			}
			Map<ObjectIdentifier, Object> mapObjects = new HashMap<ObjectIdentifier, Object>();
			return parseObject(objectElement, referencedObjectsElement,
					mapObjects);
		} catch (JDOMException e) {
			throw new InvalidXmlException("Invalid XML", e);
		} catch (IOException e) {
			throw new InvalidXmlException("Invalid XML", e);
		}
	}

	/**
	 * Método recursivo que extrai um Objeto através de um
	 * {@link org.jdom2.Element} e faz chamadas recursivas para preencher os
	 * objetos referenciados.
	 * 
	 * @param objectElement
	 *            Elemento do Objeto a ser extraido
	 * @param referencedObjectsElement
	 *            Elemento com os objetos referenciados
	 * @param objectsMap
	 *            Mapa de objetos para evitar uma nova busca
	 * @return Objeto extraído
	 */
	private static Object parseObject(Element objectElement,
			Element referencedObjectsElement,
			Map<ObjectIdentifier, Object> objectsMap) {
		String className = objectElement.getAttributeValue(XMLConstants.CLASS);
		try {
			Class<?> objectClass = Class.forName(className);
			Constructor<?> constructor = objectClass.getConstructor();
			Object object = constructor.newInstance();
			String id = objectElement.getAttributeValue(XMLConstants.ID);
			setObjectId(objectClass,
					ReflectionUtils.getInstanceVariables(objectClass), object,
					id);
			List<Element> fieldElements = objectElement
					.getChildren(XMLConstants.FIELD);
			for (Element fieldElement : fieldElements) {
				String xmlFieldType = fieldElement
						.getAttributeValue(XMLConstants.FIELD_TYPE);
				String fieldName = fieldElement
						.getAttributeValue(XMLConstants.NAME);
				Field field = ReflectionUtils.getInstanceVariable(objectClass,
						fieldName);
				if (field == null) {
					throw new FieldNotFoundException(className, fieldName);
				}
				if(fieldElement.getText().equals(XMLConstants.NULL)){
					// Campo nulo, apenas passar para o próximo
					continue;
				}
				Class<?> fieldType = field.getType();
				if (xmlFieldType.equals(XMLConstants.VALUE_FIELD_TYPE)) {
					String valueXmlField = fieldElement.getText();
					setFieldValue(objectClass, object, field,
							getTypedValue(fieldType, valueXmlField));
				} else if (xmlFieldType.equals(XMLConstants.OBJECT_FIELD_TYPE)) {
					Element referenceElement = fieldElement
							.getChild(XMLConstants.REFERENCE);
					Object referencedObject = getReferencedObject(
							referencedObjectsElement, objectsMap,
							referenceElement);
					setFieldValue(objectClass, object, field, referencedObject);

				} else if (xmlFieldType.equals(XMLConstants.ID_FIELD_TYPE)) {
					Object referencedObject = getObjectWithId(objectsMap, fieldElement,
							field, fieldType);
					setFieldValue(objectClass, object, field, referencedObject);
				} else if (xmlFieldType
						.equals(XMLConstants.OBJECT_COLLECTION_FIELD_TYPE)) {
					List<Element> referenceElements = fieldElement
							.getChildren(XMLConstants.REFERENCE);
					Collection<Object> referenceCollection = createCollection(fieldType);
					for (Element referenceElement : referenceElements) {
						Object referencedObject = getReferencedObject(
								referencedObjectsElement, objectsMap,
								referenceElement);
						referenceCollection.add(referencedObject);
					}
					setFieldValue(objectClass, object, field, referenceCollection);
				} else if (xmlFieldType
						.equals(XMLConstants.ID_COLLECTION_FIELD_TYPE)) {
					List<Element> idElements = fieldElement
							.getChildren(XMLConstants.ID);
					Collection<Object> referenceCollection = createCollection(fieldType);
					for (Element referenceElement : idElements) {
						Class<?> collectionType = getCollectionType(field.getGenericType());
						Object referencedObject = getObjectWithId(objectsMap, referenceElement,
								field, collectionType);
						referenceCollection.add(referencedObject);
					}
					setFieldValue(objectClass, object, field, referenceCollection);
				} else if (xmlFieldType
						.equals(XMLConstants.VALUE_COLLECTION_FIELD_TYPE)) {
					List<Element> valueElements = fieldElement
							.getChildren(XMLConstants.VALUE);
					Collection<Object> valueCollection = createCollection(fieldType);
					for (Element valueElement : valueElements) {
						Class<?> collectionType = getCollectionType(field.getGenericType());
						Object value = getTypedValue(collectionType, valueElement.getText());
						valueCollection.add(value);
					}
					setFieldValue(objectClass, object, field, valueCollection);
				} else if (xmlFieldType
						.equals(XMLConstants.OBJECT_ARRAY_FIELD_TYPE)) {
					List<Element> referenceElements = fieldElement
							.getChildren(XMLConstants.REFERENCE);
					Class<?> componentType = fieldType.getComponentType();
					Object referenceArray = Array.newInstance(componentType, referenceElements.size());
					for(int i=0; i<referenceElements.size(); i++){
						Object referencedObject = getReferencedObject(
								referencedObjectsElement, objectsMap,
								referenceElements.get(i));
						Array.set(referenceArray, i, referencedObject);
					}
					setFieldValue(objectClass, object, field, referenceArray);
				} else if (xmlFieldType
						.equals(XMLConstants.ID_ARRAY_FIELD_TYPE)) {
					List<Element> idElements = fieldElement
							.getChildren(XMLConstants.ID);
					Class<?> componentType = fieldType.getComponentType();
					Object idArray = Array.newInstance(componentType, idElements.size());
					for(int i=0; i<idElements.size(); i++){
						Object referencedObject = getObjectWithId(objectsMap, idElements.get(i),
								field, componentType);
						Array.set(idArray, i, referencedObject);
					}
					setFieldValue(objectClass, object, field, idArray);
				} else if (xmlFieldType
						.equals(XMLConstants.VALUE_ARRAY_FIELD_TYPE)) {
					List<Element> valueElements = fieldElement
							.getChildren(XMLConstants.VALUE);
					Class<?> componentType = fieldType.getComponentType();
					Object valueArray = Array.newInstance(componentType, valueElements.size());
					for(int i=0; i<valueElements.size(); i++){
						Object value = getTypedValue(componentType, valueElements.get(i).getText());
						Array.set(valueArray, i, value);
					}
					setFieldValue(objectClass, object, field, valueArray);
				}
			}
			return object;
		} catch (ClassNotFoundException e) {
			throw new ObjectClassNotFoundException(e);
		} catch (InstantiationException e) {
			throw new ConstructionException(className, e);
		} catch (IllegalAccessException e) {
			throw new ConstructionException(className, e);
		} catch (IllegalArgumentException e) {
			throw new ConstructionException(className, e);
		} catch (InvocationTargetException e) {
			throw new ConstructionException(className, e);
		} catch (NoSuchMethodException e) {
			throw new ConstructionException(className, e);
		}
	}

	private static Collection<Object> createCollection(Class<?> type) {
		if(List.class.isAssignableFrom(type)){
			if(List.class != type){
				throw new ImplementationCollectionException(type.getName());
			}
			return new ArrayList<Object>();
		}
		else if(Set.class.isAssignableFrom(type)){
			if(Set.class != type){
				throw new ImplementationCollectionException(type.getName());
			}
			return new HashSet<Object>();
		}
		throw new CollectionTypeNotSupportedException(type.getName());
	}

	/**
	 * Método responsável por criar um novo objeto e ajustar apenas seu ID ou buscá-lo 
	 * no mapa de objetos, caso ele já tenha sido criado. Ele espera um elemento ID 
	 * ou um elemento FIELD com um atributo ID.
	 * 
	 * @param objectsMap Mapa com os objetos já criados
	 * @param element Elemento ID ou FIELD com atributo ID
	 * @param field Campo a ser colocado
	 * @param objectType Tipo do novo objeto a ser criado
	 * @return
	 */
	private static Object getObjectWithId(	Map<ObjectIdentifier, Object> objectsMap, Element element, Field field, Class<?> objectType) {
		String id = null;
		if (element.getName().equals(XMLConstants.ID)) {
			id = element.getText();
		} else {
			id = element.getAttributeValue(XMLConstants.ID);
		}
		ObjectIdentifier objectIdenfier = new ObjectIdentifier(
				objectType, id);
		if (objectsMap.containsKey(objectIdenfier)) {
			return objectsMap.get(objectIdenfier);
		}
		try {
			Constructor<?> referenceConstructor = objectType
					.getConstructor();
			Object newObject = referenceConstructor.newInstance();
			setObjectId(objectType,
					ReflectionUtils.getInstanceVariables(objectType),
					newObject, id);
			objectsMap.put(objectIdenfier, newObject);
			return newObject;
		} catch (InstantiationException e) {
			throw new ConstructionException(objectType.getName(), e);
		} catch (IllegalAccessException e) {
			throw new ConstructionException(objectType.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new ConstructionException(objectType.getName(), e);
		} catch (InvocationTargetException e) {
			throw new ConstructionException(objectType.getName(), e);
		} catch (NoSuchMethodException e) {
			throw new ConstructionException(objectType.getName(), e);
		}
	}

	/**
	 * Método responsável por extrair um objeto referenciado através de seu
	 * {@link org.jdom2.Element} ou através do mapa de objetos (caso ele tenha
	 * sido extraído antes) e retorná-lo.<br>
	 * A chamada recursiva ao método {@link #parseObject(Element, Element, Map)}
	 * está nesse método (que é chamado pelo parseObject).
	 * 
	 * @param referencedObjectsElement
	 *            Elemento que contém os objetos referenciados
	 * @param mapObjects
	 *            Mapa de objetos já extraidos
	 * @param referenceElement
	 *            Elemento do objeto referenciado
	 * @return Objeto referenciado
	 * @throws ClassNotFoundException
	 * 
	 */
	private static Object getReferencedObject(Element referencedObjectsElement,
			Map<ObjectIdentifier, Object> mapObjects, Element referenceElement)
			throws ClassNotFoundException {
		if (referenceElement == null) {
			throw new InvalidXmlException("Reference element not found");
		}
		String referenceClassName = referenceElement
				.getAttributeValue(XMLConstants.CLASS);
		String referenceId = referenceElement
				.getAttributeValue(XMLConstants.ID);
		ObjectIdentifier referenceIdenfier = new ObjectIdentifier(
				Class.forName(referenceClassName), referenceId);
		Object referencedObject = null;
		if (mapObjects.containsKey(referenceIdenfier)) {
			referencedObject = mapObjects.get(referenceIdenfier);
		} else {
			Element referencedObjectElement = getReferencedObjectElement(
					referenceIdenfier, referencedObjectsElement);
			referencedObject = parseObject(referencedObjectElement,
					referencedObjectsElement, mapObjects);
			mapObjects.put(referenceIdenfier, referencedObject);
		}
		return referencedObject;
	}

	/**
	 * Método responsável por encontrar o {@link org.jdom2.Element} do objeto
	 * referenciado usando seu
	 * {@link br.com.cybereagletechnology.eaglesync.core.ObjectIdentifier} para
	 * localizá-lo.
	 * 
	 * @param referenceIdentifier
	 *            Identificador do objeto referenciado
	 * @param referencedObjectsElement
	 *            Elemento contendo todos os elementos de objetos referenciados
	 * @return Elemento do objeto referenciado
	 */
	private static Element getReferencedObjectElement(
			ObjectIdentifier referenceIdentifier,
			Element referencedObjectsElement) {
		for (Element element : referencedObjectsElement
				.getChildren(XMLConstants.OBJECT)) {
			if (element.getAttributeValue(XMLConstants.CLASS).equals(
					referenceIdentifier.getCls().getName())
					&& element.getAttributeValue(XMLConstants.ID).equals(
							referenceIdentifier.getId())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Método responsável por converter um valor de {@link java.lang.String}
	 * para o tipo especificado pelo parâmetro type.
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	private static Object getTypedValue(Class<?> type, String value) {
		if (String.class.isAssignableFrom(type)) {
			return value;
		} else if (Number.class.isAssignableFrom(type)) {
			if (Byte.class.isAssignableFrom(type)) {
				return Byte.valueOf(value);
			} else if (Double.class.isAssignableFrom(type)) {
				return Double.valueOf(value);
			} else if (Float.class.isAssignableFrom(type)) {
				return Float.valueOf(value);
			} else if (Integer.class.isAssignableFrom(type)) {
				return Integer.valueOf(value);
			} else if (Long.class.isAssignableFrom(type)) {
				return Long.valueOf(value);
			} else if (Short.class.isAssignableFrom(type)) {
				return Short.valueOf(value);
			}
		} else if (Boolean.class.isAssignableFrom(type)) {
			return Boolean.parseBoolean(value);
		} else if (Character.class.isAssignableFrom(type)) {
			return value.charAt(0);
		} else if (Date.class.isAssignableFrom(type)) {
			DateFormat dateFormat = DateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT, Locale.US);
			try {
				return dateFormat.parse(value);
			} catch (ParseException e) {
				throw new InvalidDateException(value, e);
			}
		}
		throw new InvalidTypeException("Invalid type: " + type.getName());
	}

	/**
	 * Método responsável por converter um valor de um tipo comum para
	 * {@link java.lang.String}.<br>
	 * Todos os tipos têm sua representação padrão de {@link java.lang.String}
	 * retornada através de toString(), com exceção de Date que é formatada com
	 * o padrão SHORT e considerado o {@link java.util.Locale} US.
	 * 
	 * @param value
	 *            Objeto a ser transformado em String
	 * @return
	 */
	private static String getStringValue(Object value) {
		if (value instanceof Date) {
			DateFormat dateFormat = DateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT, Locale.US);
			return dateFormat.format((Date) value);
		}
		return value.toString();
	}

	/**
	 * Garante que haverá apenas um objeto referenciado no
	 * referencedObjectsElement e retorna uma referência para ele.
	 * 
	 * @param referencedObjectsElement
	 *            Elemento que carrega os objetos referenciados
	 * @param referencedObjectsSet
	 *            Set usado para garantir que não haja repetição de objetos
	 *            referenciados
	 * @param referenceType
	 *            Tipo da referência
	 * @param value
	 *            Objeto a ser referenciado
	 * @return Elemento de referência para o objeto
	 */
	private static Element getReference(Element referencedObjectsElement,
			Set<ObjectIdentifier> referencedObjectsSet, Class<?> referenceType,
			Object value) {
		Element referenceElement = new Element(XMLConstants.REFERENCE);
		referenceElement.setAttribute(XMLConstants.CLASS,
				referenceType.getName());
		Object referenceId = getObjectId(referenceType,
				ReflectionUtils.getInstanceVariables(referenceType), value);
		referenceElement.setAttribute(XMLConstants.ID, referenceId.toString());

		ObjectIdentifier objectIdentifier = new ObjectIdentifier(referenceType,
				getStringValue(referenceId));
		if (!referencedObjectsSet.contains(objectIdentifier)) {
			Element referencedObjectElement = parseElement(value,
					referencedObjectsElement, referencedObjectsSet);
			referencedObjectsElement.addContent(referencedObjectElement);
			referencedObjectsSet.add(objectIdentifier);
		}
		return referenceElement;
	}

	/**
	 * Método responsável por ajustar o texto de um {@link org.jdom2.Element}
	 * para o valor do parâmetro ou <b>null</b>.
	 * 
	 * @param element
	 *            Elemento que terá seu texto ajustado
	 * @param value
	 *            Valor a ser utilizado
	 */
	private static void setElementText(Element element, Object value) {
		element.setText(value != null ? getStringValue(value)
				: XMLConstants.NULL);
	}

	/**
	 * Adiciona um {@link org.jdom2.Element} com um determinado valor ao
	 * {@link org.jdom2.Element} recebido como parâmetro
	 * 
	 * @param element
	 *            Elemento terá um elemento com um valor adicionado
	 * @param value
	 *            Valor a ser adicionado
	 */
	private static void addValueElement(Element element, Object value) {
		Element valueElement = new Element(XMLConstants.VALUE);
		valueElement.setText(value != null ? value.toString()
				: XMLConstants.NULL);
		element.addContent(valueElement);
	}

	/**
	 * Método responsável por adicionar um {@link org.jdom2.Element} com o ID do
	 * objeto ao {@link org.jdom2.Element} recebido como parâmetro.
	 * 
	 * @param element
	 *            Elemento que terá um elemento de ID adicionado
	 * @param type
	 *            Classe de onde se obterá o ID
	 * @param value
	 *            Objeto de onde se obterá o ID
	 */
	private static void addIdElement(Element element, Class<?> type,
			Object value) {
		Object valueId = getObjectId(type,
				ReflectionUtils.getInstanceVariables(type), value);
		if (isCommonType(valueId)) {
			Element idElement = new Element(XMLConstants.ID);
			idElement.setText(valueId.toString());
			element.addContent(idElement);
		} else if (valueId == null) {
			throw new NullIdException(type.getName());
		} else {
			throw new IdTypeNotSupportedException(type.getName());
		}
	}

	/**
	 * Método responsável por encontrar qual o tipo de determinada
	 * {@link java.util.Collection}
	 * 
	 * @param fieldGenericType
	 *            Tipo do campo a ser buscado
	 * @return Tipo parametrizado na {@link java.util.Collection}
	 */
	private static Class<?> getCollectionType(Type fieldGenericType) {
		if (fieldGenericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
			return (Class<?>) parameterizedType.getActualTypeArguments()[0];
		}
		// Nunca acontecerá:
		return null;
	}

	/**
	 * Método responsável por extrair o ID de um objeto com base em suas
	 * variáveis de instância. Ele busca por uma variável de instância anotada
	 * com {@link br.com.cybereagletechnology.eaglesync.metadata.ServerId} e retorna uma
	 * String representando seu valor. Não é permitido marcar um outro bean como
	 * identificador.
	 * 
	 * @param cls
	 *            Classe do objeto
	 * @param fields
	 *            Campos usados na busca
	 * @param source
	 *            Objeto usado na busca
	 * @return Identificador do objeto
	 */
	private static String getObjectId(Class<?> cls, List<Field> fields,
			Object source) {
		for (Field field : fields) {
			for (Annotation annotation : field.getAnnotations()) {
				if (annotation instanceof ServerId) {
					Object id = null;
					id = extractFieldValue(cls, source, field);
					if (id == null) {
						throw new NullIdException(cls.getName());
					}
					// Verifica se id é instância de um dos tipos comuns
					if (isCommonType(id)) {
						// Remove o ID da lista de campos
						fields.remove(field);
						return id.toString();
					} else {
						throw new IdTypeNotSupportedException(cls.getName());
					}
				}
			}
		}
		throw new IdNotFoundException(cls.getName());
	}

	/**
	 * Método responsável por ajustar o ID de um objeto com base em suas
	 * variáveis de instância. Ele busca por uma variável de instância anotada
	 * com {@link br.com.cybereagletechnology.eaglesync.metadata.ServerId} e ajusta seu
	 * valor. Não é permitido marcar um outro bean como identificador.
	 * 
	 * @param cls
	 *            Classe do objeto
	 * @param fields
	 *            Campos usados na busca
	 * @param source
	 *            Objeto usado na busca
	 * @param id
	 *            ID a ser ajustado
	 */
	private static void setObjectId(Class<?> cls, List<Field> fields,
			Object source, String id) {
		for (Field field : fields) {
			for (Annotation annotation : field.getAnnotations()) {
				if (annotation instanceof ServerId) {
					Object typedId = getTypedValue(field.getType(), id);
					setFieldValue(cls, source, field, typedId);
					return;
				}
			}
		}
		throw new IdNotFoundException(cls.getName());
	}

	/**
	 * Métido responsável por pegar o valor de um campo. Ele tenta primeiro
	 * através do getter. Caso não consiga, ele torna o campo acessível e pega
	 * seu valor.
	 * 
	 * @param cls
	 *            Classe que possui o campo
	 * @param source
	 *            Objecto de onde será extraído o valor
	 * @param field
	 *            Campo a ser usado
	 * @return
	 */
	private static Object extractFieldValue(Class<?> cls, Object source,
			Field field) {
		Object fieldValue = null;
		try {
			fieldValue = ReflectionUtils.get(cls, field, source);
		} catch (Exception e) {
			// Caso que acontecerá no caso do getter retornar
			// uma exceção ou não haver nenhum getter
			if (!Modifier.isPublic(field.getModifiers())) {
				field.setAccessible(true);
			}
			try {
				fieldValue = field.get(source);
			} catch (Exception e1) {
				// Nunca acontecerá
			}
		}
		return fieldValue;
	}

	/**
	 * Métido responsável por ajustar o valor de um campo. Ele tenta primeiro
	 * através do setter. Caso não consiga, ele torna o campo acessível e ajusta
	 * seu valor.
	 * 
	 * @param cls
	 *            Classe que possui o campo
	 * @param source
	 *            Objecto que terá seu campo ajustado
	 * @param field
	 *            Campo a ser usado
	 * @param value
	 *            Valor a ser usado
	 * @return
	 */
	private static void setFieldValue(Class<?> cls, Object source, Field field,
			Object value) {
		try {
			ReflectionUtils.set(cls, field, source, value);
		} catch (Exception e) {
			// Caso que acontecerá no caso do setter retornar
			// uma exceção ou não haver nenhum setter
			if (!Modifier.isPublic(field.getModifiers())) {
				field.setAccessible(true);
			}
			try {
				field.set(source, value);
			} catch (Exception e1) {
				// Nunca deve ser ancançado
			}
		}
	}

	/**
	 * Método responsável por verificar se determinado objeto é de um tipo
	 * comum. Entende-se por comum os tipos numéricos (extends
	 * {@link java.lang.Number}), {@link java.lang.Boolean},
	 * {@link java.lang.String}, {@link java.lang.Character} e
	 * {@link java.util.Date}.
	 * 
	 * @param obj
	 *            Objeto a ser testado
	 * @return true se for de um tipo comum, false se não.
	 */
	private static boolean isCommonType(Object obj) {
		return Number.class.isInstance(obj) || Boolean.class.isInstance(obj)
				|| String.class.isInstance(obj)
				|| Character.class.isInstance(obj)
				|| Date.class.isInstance(obj);
	}

	/**
	 * Método responsável por verificar se determinado tipo é um tipo comum.
	 * Entende-se por comum os tipos numéricos (extends {@link java.lang.Number}
	 * ), {@link java.lang.Boolean}, {@link java.lang.String},
	 * {@link java.lang.Character} e {@link java.util.Date}.
	 * 
	 * @param obj
	 *            Objeto a ser testado
	 * @return true se for de um tipo comum, false se não.
	 */
	private static boolean isCommonType(Class<?> cls) {
		return Number.class.isAssignableFrom(cls)
				|| Boolean.class.isAssignableFrom(cls)
				|| String.class.isAssignableFrom(cls)
				|| Character.class.isAssignableFrom(cls)
				|| Date.class.isAssignableFrom(cls);
	}

	/**
	 * Método responsável por verificar se determinado objeto é de um tipo
	 * Collection.
	 * 
	 * @param obj
	 *            Objeto a ser testado
	 * @return true se for de um tipo comum, false se não.
	 */
	private static boolean isCollectionType(Object obj) {
		return Collection.class.isInstance(obj);
	}

	/**
	 * Método responsável por verificar se determinado tipo é um Array.
	 * 
	 * @param obj
	 *            Objeto a ser testado
	 * @return true se for de um tipo comum, false se não.
	 */
	private static boolean isArrayType(Class<?> cls) {
		return cls.isArray();
	}

	/**
	 * Método responsável por remover os campos marcados com
	 * {@link br.com.cybereagletechnology.eaglesync.metadata.Transient} da lista
	 * de campos.
	 * 
	 * @param fields
	 */
	private static void removeTransientFields(List<Field> fields) {
		for (int i = 0; i < fields.size(); i++) {
			for (Annotation annotation : fields.get(i).getAnnotations()) {
				if (annotation instanceof Transient) {
					fields.remove(i);
					i--;
				}
			}
		}
	}
}
