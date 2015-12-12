/**
 * DO NOT MODIFY THIS CODE
 *
 * Place all of your changes in Product.java
 *
 * It has been machine generated from fixtures and your changes will be
 * lost if anything new needs to be added to the API.
 **/
// Last Generated: 2011-09-26T15:53:49-04:00
package com.shopify.api.resources;

import java.util.List;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This code has been machine generated by processing the single entry
 * fixtures found from the Shopify API Documentation
 */

public class MGProduct extends ShopifyResource {

	@JsonProperty("body_html")
	public String getBodyHtml() {
		return (String)getAttribute("body_html");
	}
	@JsonProperty("body_html")
	public void setBodyHtml(String _body_html) {
		setAttribute("body_html", _body_html);
	}

	@JsonProperty("handle")
	public String getHandle() {
		return (String)getAttribute("handle");
	}
	@JsonProperty("handle")
	public void setHandle(String _handle) {
		setAttribute("handle", _handle);
	}

	@JsonProperty("images")
	public List<Image> getImages() {
		return (List<Image>)getAttribute("images");
	}
	@JsonProperty("images")
	public void setImages(List<Image> _images) {
		setAttribute("images", _images);
	}

	@JsonProperty("options")
	public List<Option> getOptions() {
		return (List<Option>)getAttribute("options");
	}
	@JsonProperty("options")
	public void setOptions(List<Option> _options) {
		setAttribute("options", _options);
	}

	@JsonProperty("product_type")
	public String getProductType() {
		return (String)getAttribute("product_type");
	}
	@JsonProperty("product_type")
	public void setProductType(String _product_type) {
		setAttribute("product_type", _product_type);
	}

	@JsonProperty("published_at")
	public String getPublishedAt() {
		return (String)getAttribute("published_at");
	}
	@JsonProperty("published_at")
	public void setPublishedAt(String _published_at) {
		setAttribute("published_at", _published_at);
	}

	@JsonProperty("tags")
	public String getTags() {
		return (String)getAttribute("tags");
	}
	@JsonProperty("tags")
	public void setTags(String _tags) {
		setAttribute("tags", _tags);
	}

	@JsonProperty("template_suffix")
	public String getTemplateSuffix() {
		return (String)getAttribute("template_suffix");
	}
	@JsonProperty("template_suffix")
	public void setTemplateSuffix(String _template_suffix) {
		setAttribute("template_suffix", _template_suffix);
	}

	@JsonProperty("title")
	public String getTitle() {
		return (String)getAttribute("title");
	}
	@JsonProperty("title")
	public void setTitle(String _title) {
		setAttribute("title", _title);
	}

	@JsonProperty("variants")
	public List<Variant> getVariants() {
		return (List<Variant>)getAttribute("variants");
	}
	@JsonProperty("variants")
	public void setVariants(List<Variant> _variants) {
		setAttribute("variants", _variants);
	}

	@JsonProperty("vendor")
	public String getVendor() {
		return (String)getAttribute("vendor");
	}
	@JsonProperty("vendor")
	public void setVendor(String _vendor) {
		setAttribute("vendor", _vendor);
	}

	@JsonProperty("published_scope")
	public String getPublishedScope() {
		return (String)getAttribute("published_scope");
	}
	@JsonProperty("published_scope")
	public void setPublishedScope(String _published_scope) {
		setAttribute("published_scope", _published_scope);
	}	
	
	@JsonProperty("image")
	public Image getImage() {
		return (Image)getAttribute("image");
	}
	@JsonProperty("image")
	public void setImage(Image _image) {
		setAttribute("image", _image);
	}	
}
