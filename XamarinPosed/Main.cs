namespace XamarinPosed
{
    [Activity(Name = "xamarin.posed.Main", Label = "@string/app_name", MainLauncher = true)]
    public partial class Main : Activity
    {
        protected override void OnCreate(Bundle? savedInstanceState)
        {
            base.OnCreate(savedInstanceState);

            // Set our view from the "main" layout resource
            SetContentView(Resource.Layout.activity_main);
        }
    }
}