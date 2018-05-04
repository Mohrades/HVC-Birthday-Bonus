package handlers;

import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.MessageSource;

import com.google.common.base.Splitter;

import domain.models.HVC;
import domain.models.USSDRequest;
import filter.MSISDNValidator;
import product.ProductProperties;

@SuppressWarnings("unused")
public class USSDFlow {

	public USSDFlow() {

	}

	public Map<String, Object> validate(USSDRequest ussd, HVC hvc, Document document, ProductProperties productProperties, MessageSource i18n) {
		// on crée le modèle de la vue à afficher
		Map<String, Object> modele = new HashMap<String, Object>();
		// initialization
		modele.put("status", -1);

		// on crée le modèle de l'arborescence
		StringJoiner tree = new StringJoiner(".", ".", "");
		tree.setEmptyValue("");

		try {
			// USSD(int id, long sessionId, String msisdn, String input, int step, Date last_update_time)
			// 250*1**263*abc*1*97975506  ==>  [250, 1, , 263, abc, 1, 97975506]
			// List<String> inputs = Splitter.onPattern("[.|,|;]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());
			// List<String> inputs = Splitter.onPattern("[*]").trimResults().splitToList(ussd.getInput());
			List<String> inputs = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());

			int index = 0;
			Element currentState = null;

			transitions : for(String input : inputs) {
				if((input == null) || (input.isEmpty()) ||(input.length() == 0)) {
					return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
				}

				// on-entry : verify service code
				if(index == 0) {
					if(document.getRootElement().getName().equals("SERVICE-CODE-"  + input)) {
						currentState = (document.getRootElement()).getChild("menu");
						index++;
					}
					else {
						return handleInvalidInput(i18n.getMessage("service.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
				}
				// transition : verify each step of the flow
				else {
					if(hasChildren(currentState)) {
						@SuppressWarnings("rawtypes")
						List children = currentState.getChildren("input");
						Element choice = currentState.getChild("choice-" + input);

						if(choice != null) {
							children = currentState.getChildren();

							@SuppressWarnings("rawtypes")
							ListIterator iterator = children.listIterator();
							int step = 0;

							while (iterator.hasNext()) {
								step++;
								Element element = (Element) iterator.next();

								if(element.getName().equals("choice-" + input)) {
									tree.add(step + "");
									break;
								}
							}

							currentState = choice;
							continue transitions;
						}
						else if(children.isEmpty()) {
							return handleInvalidInput(i18n.getMessage("integer.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
						}
						else {
							children = currentState.getChildren();

							@SuppressWarnings("rawtypes")
							ListIterator iterator = children.listIterator();
							int step = 0;

							while (iterator.hasNext()) {
								step++;
								Element element = (Element) iterator.next();

								if(element.getName().startsWith("choice-")) {

								}
								else if(element.getName().equals("input")) {
									if(element.getAttributeValue("type").equals("static")) {
										if(input.equals(element.getAttributeValue("value"))) {
											currentState = element;
											tree.add(step + "");
											continue transitions;
										}
										else {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}
										}
									}
									else if(element.getAttributeValue("type").equals("text")) {
										if(input.isEmpty()) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("argument.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}
										}
										else {
											currentState = element;
											tree.add(step + "");
											continue transitions;
										}
									}
									else if(element.getAttributeValue("type").equals("number")) {
										try {
											Long.parseLong(input);
											currentState = element;
											tree.add(step + "");
											continue transitions;

										} catch(NullPointerException|NumberFormatException ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("integer.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}

										} catch(Throwable ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("integer.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}
										}
									}
									else if(element.getAttributeValue("type").equals("msisdn")) {
										try {
											String msisdn = Long.parseLong(input) + "";

											if((element.getAttributeValue("ton").equals("International") && (msisdn.startsWith(productProperties.getMcc() + "")) && (((productProperties.getMcc() + "").length() + productProperties.getMsisdn_length()) == msisdn.length())) || ((element.getAttributeValue("ton").equals("National")) && (productProperties.getMsisdn_length() == msisdn.length()))) {
											/*if((element.getAttributeValue("ton").equals("International")) || ((element.getAttributeValue("ton").equals("National")) && (webAppProperties.getMsisdn_length() == msisdn.length()))) {*/
												if((element.getAttributeValue("network") == null) || (element.getAttributeValue("network").isEmpty()) || (element.getAttributeValue("network").equals("off"))) {
													currentState = element;
													tree.add(step + "");
													continue transitions;
												}
												else if(element.getAttributeValue("network").equals("on")) {
													if(((element.getAttributeValue("ton").equals("National")) && (new MSISDNValidator()).onNet(productProperties, productProperties.getMcc() + "" + msisdn)) || ((element.getAttributeValue("ton").equals("International")) && (new MSISDNValidator()).onNet(productProperties, msisdn))) {
														currentState = element;
														tree.add(step + "");
														continue transitions;
													}
													else {
														if(children.size() == 1) {
															return handleInvalidInput(i18n.getMessage("msisdn.onnet.required", new Object[] {productProperties.getGsm_name()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
														}
													}
												}
											}
											else {
												if(children.size() == 1) {
													return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
												}
											}

										} catch(NullPointerException|NumberFormatException ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}

										} catch(Throwable ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
											}
										}
									}									
								}
							}

							return handleInvalidInput(i18n.getMessage("argument.required", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
						}
					}
					else {
						return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
				}
			}

			// StringJoiner is used internally by static String.join().
			// String.join("-", "2015", "10", "31" ); // Join String by a delimiter ==> 2015-10-31
			// correct subscriber inputs in the good and recommended format
			ussd.setInput(String.join("*", inputs)); // Join a List by a delimiter

			// on-transition : view-state
			if(hasChildren(currentState)) {
				String transitions = tree.toString();
				transitions = transitions.trim();

				modele.put("status", 1);

				if(transitions.isEmpty()) {
					// volume voice
					long volume_voice = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));
					volume_voice = (long) (((double)volume_voice) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
					
					// volume data
					long volume_data = Long.parseLong(productProperties.getData_volume().get(hvc.getSegment() - 1));
					volume_data = (long) (((double)volume_data) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));
					
					if(volume_data >= 1024) {
						modele.put("message", i18n.getMessage("menu" + transitions, new Object [] {volume_voice/(60*100), new Formatter().format("%.2f", ((double)volume_data)/1024), "Go"}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
					else {
						modele.put("message", i18n.getMessage("menu" + transitions, new Object [] {volume_voice/(60*100), volume_data, "Mo"}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
				}
				else if(transitions.equalsIgnoreCase(".1")) {
					long volume = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));
					volume = (long) (((double)volume) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));

					modele.put("message", i18n.getMessage("menu" + transitions, new Object [] {volume/(60*100)}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
				}
				else if(transitions.equalsIgnoreCase(".2")) {
					long volume = Long.parseLong(productProperties.getData_volume().get(hvc.getSegment() - 1));
					volume = (long) (((double)volume) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));

					if(volume >= 1024) {
						modele.put("message", i18n.getMessage("menu" + transitions, new Object [] {new Formatter().format("%.2f", ((double)volume)/1024), "Go"}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
					else {
						modele.put("message", i18n.getMessage("menu" + transitions, new Object [] {volume, "Mo"}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
					}
				}
			}
			// on-end : end-state
			else {
				modele.put("status", 0);
			}

		} catch(NullPointerException ex) {
			handleException(modele, i18n, hvc);

		} catch(Throwable th) {
			handleException(modele, i18n, hvc);
		}

		return modele;
	}

	public void handleException(Map<String, Object> modele, MessageSource i18n, HVC hvc) {
		modele.put("status", -1);
		modele.put("message", i18n.getMessage("request.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));
	}

	public Map<String, Object> handleInvalidInput(String message) {
		// on crée le modèle de la vue à afficher
		Map<String, Object> modele = new HashMap<String, Object>();

		modele.put("status", -1);
		modele.put("message", message);
		return modele;
	}

	public boolean hasChildren(Element currentSate) {
		return (currentSate == null) ? false : (currentSate.getChildren().size() > 0);
	}

}
