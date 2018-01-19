package eu.vmpay.weathermate;

/**
 * Created by andrew on 1/19/18.
 */

public interface BasePresenter<T>
{
	/**
	 * Binds presenter with a view when resumed. The Presenter will perform initialization here.
	 *
	 * @param view the view associated with this presenter
	 */
	void takeView(T view);

	/**
	 * Drops the reference to the view when destroyed
	 */
	void dropView();
}
